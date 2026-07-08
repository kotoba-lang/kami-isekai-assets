;; Pixel-level proof that a kami-isekai-assets composed monster ACTUALLY renders as real GPU
;; pixels through kami.scene2d + kami.sprite-gpu (kotoba-lang/webgpu) — the same pipeline
;; network-isekai's games already draw every frame through. This closes the gap the 2026-07
;; maturity audit found: this library's `:sprite` EDN had never been rendered to a screen by
;; anything in ITS OWN test/demo suite (network-isekai consumes it, but that lives in a
;; separate repo/org, not this one).
;;
;; Reuses kotoba-lang/webgpu's own real-headless-Chromium/WebGL2 harness (`kami.playwright`)
;; verbatim, in the same style as that repo's test/playwright_frame_test.clj /
;; playwright_anim_test.clj: pack the GPU quads, compile+link the SAME sprite-SDF GLSL fixture
;; those tests use, draw an instanced quad pass into a real WebGL2 canvas, then `readPixels`
;; and count actual colour matches — a check that fails if the render pipeline breaks, not just
;; "compiles"/"didn't throw".
;;
;; This is a .clj (not .cljc) test on purpose: `kami.playwright`/`kami.scene2d`/`kami.sprite-gpu`
;; live in the sibling kotoba-lang/webgpu repo, not this repo's own deps.edn (kami-isekai-assets
;; stays a pure-EDN library with zero deps for everyone who doesn't want the render pipeline).
;; Run via `bb render-test` (this repo's bb.edn) — that task shells out with the working
;; directory set to a sibling kotoba-lang/webgpu checkout, because kami.playwright's bridge
;; script and the GLSL fixtures below are read via cwd-relative paths (matching every other
;; playwright_*_test.clj in that repo) — see bb.edn's `render-test` task doc for the exact
;; sibling-checkout layout this expects (kotoba-lang/{kami-isekai-assets,webgpu,expr} as
;; siblings, the same layout the west-managed superproject already checks these repos out in).
(ns render-pixel-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [kami.playwright :as pw]
            [kami.scene2d :as s2]
            [kami.sprite-gpu :as sg]
            [kami.isekai.monsters :as monsters]
            [kami.isekai.render-adapter :as radapt]
            [cheshire.core :as json]))

(defn- glsl [f] (slurp (str "fixtures/glsl/" f)))

;; The simplest catalog entry, on purpose (ADR-pending maturity pass — keep the first render
;; PoC tight): a single monster, not a full multi-part character, so the pixel assertions below
;; can check a specific known fill colour without re-deriving the whole chargen palette math.
(def slime (monsters/compose-slime))

(defn- render
  "Render `sprite` (a kami.isekai `:sprite` primitive vector) through kami.isekai.render-adapter
   → kami.scene2d/frame-quads → kami.sprite-gpu's packed instances → a real WebGL2 canvas, and
   read back {:green :dark :total :n}. Broken out so the break-it/fix-it demonstration (see
   CHANGELOG / PR description) can call it with a deliberately-wrong sprite too."
  [sprite]
  (let [{:keys [scene snap]} (radapt/preset->scene "slime" {:sprite sprite} [0 0])
        {:keys [quads]} (s2/frame-quads scene snap [] 0 640 480)
        js (str "const PV=" (json/generate-string (glsl "sprite.vert")) ",PF=" (json/generate-string (glsl "sprite.frag")) ";"
                "const data=new Float32Array(" (json/generate-string (vec (sg/pack-instances quads))) ");const N=" (count quads) ";"
                "const W=640,H=480;const gl=Object.assign(document.createElement('canvas'),{width:W,height:H}).getContext('webgl2');"
                "function c(t,s){const x=gl.createShader(t);gl.shaderSource(x,s);gl.compileShader(x);return x;}"
                "const p=gl.createProgram();gl.attachShader(p,c(gl.VERTEX_SHADER,PV));gl.attachShader(p,c(gl.FRAGMENT_SHADER,PF));gl.linkProgram(p);"
                "const vao=gl.createVertexArray();gl.bindVertexArray(vao);const ib=gl.createBuffer();gl.bindBuffer(gl.ARRAY_BUFFER,ib);gl.bufferData(gl.ARRAY_BUFFER,data,gl.STATIC_DRAW);"
                "[[0,2,0],[1,2,8],[2,1,16],[3,1,20],[4,4,24]].forEach(([l,n,o])=>{gl.enableVertexAttribArray(l);gl.vertexAttribPointer(l,n,gl.FLOAT,false,48,o);gl.vertexAttribDivisor(l,1);});"
                "const ub=gl.createBuffer();gl.bindBuffer(gl.UNIFORM_BUFFER,ub);gl.bufferData(gl.UNIFORM_BUFFER,new Float32Array([W,H,0,0]),gl.STATIC_DRAW);"
                "gl.uniformBlockBinding(p,gl.getUniformBlockIndex(p,'U_block_0Vertex'),0);gl.bindBufferBase(gl.UNIFORM_BUFFER,0,ub);"
                ;; Clear to a NEUTRAL MID-GRAY (not black/white) on purpose: the slime's near-black
                ;; eye dots (~rgb(20,20,25)) would be indistinguishable from a black clear colour
                ;; (both read as \"all channels < 40\"), which would make the :dark count below pass
                ;; even if the eyes never actually drew — a mid-gray background can't be mistaken
                ;; for either the green body or the near-black eyes, so both counts are real signal.
                "gl.useProgram(p);gl.viewport(0,0,W,H);gl.clearColor(0.5,0.5,0.5,1);gl.clear(gl.COLOR_BUFFER_BIT);"
                "gl.enable(gl.BLEND);gl.blendFunc(gl.ONE,gl.ONE_MINUS_SRC_ALPHA);"
                "if (N>0) gl.drawArraysInstanced(gl.TRIANGLES,0,6,N);"
                "const b=new Uint8Array(W*H*4);gl.readPixels(0,0,W,H,gl.RGBA,gl.UNSIGNED_BYTE,b);"
                ;; slime body fill ≈ watercolor([0.42 0.78 0.40], 0.08) ≈ rgb(118,201,110) — a
                ;; clearly green pixel (g well above both r and b); the two eye dots are near-
                ;; black (r,g,b all < 40) opaque circles inset into the body.
                "let green=0,dark=0,total=0;for(let i=0;i<b.length;i+=4){const r=b[i],g=b[i+1],bl=b[i+2],a=b[i+3];if(a>0)total++;"
                "if(g>150&&g>r+30&&g>bl+30&&a>0) green++; if(r<40&&g<40&&bl<40&&a>200) dark++;}"
                "return {green, dark, total, n:N};")]
    (pw/eval-page js)))

(deftest slime-preset-renders-real-pixels
  (let [r (render (:sprite slime))]
    (println "  kami-isekai-assets slime →GPU render:" r)
    (is (> (:n r) 0) "compose-slime's :sprite produced at least one GPU quad")
    (is (> (:green r) 3000)
        "the slime's watercolour-green body fill is actually on screen (not just present as data)")
    (is (> (:dark r) 20)
        "the two dark eye-dot sub-primitives are also on screen (whole recipe drew, not just the first primitive)")))

(let [{:keys [fail error]} (run-tests 'render-pixel-test)]
  (when (pos? (+ fail error))
    (throw (ex-info "kami-isekai-assets → GPU pixel render failed" {:fail fail :error error}))))
