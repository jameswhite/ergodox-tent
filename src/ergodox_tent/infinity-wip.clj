(ns ergodox-tent.core
  (:use [scad-clj.scad])
  (:use [scad-clj.model]))

;; Note that I did this using the values from lister's case design.
;; I got all the numbers from the SVG, which represents all values
;; in pixels, with 90 pixels per inch.  At the end, I scale everything
;; to be in mm.

;(def rect-depth 441.894) ; the length of the high edge 5 7/16" (5.4375") or 13.8cm (the infinity is 12.15cm here, maybe a proportional change will help (441.894/13.8)*12.15 = 389.0588 )
(def rect-depth 385.515)
(def rect-width 522.174) ; the lenth of the tines 3.5" 8.9cm (but probably x2
(def rect-height 100)    ; 4cm, but we only use part of this 1 5/8"

(def curve-radius 25.803)

(def total-width (+ rect-width (* 2 curve-radius)))
(def total-depth (+ rect-depth (* 2 curve-radius)))

(def slope (/ Math/PI 12)) ;pi/(rise/run) = 12 ; I measure the rise as 2.105cm and the run as 8.1cm; which comes out to 12.088. maybe?

(def screw-radius (/ 11.63 2)); 1/8" 0.6mm diameter
(def nut-radius 11.63)

(def nut-hole
  (cylinder nut-radius (* 5 rect-height)))

(def main-base
  (difference
   (translate [0 0 (/ rect-height 2)]
              (union
               (cube total-width rect-depth rect-height)
               (cube rect-width total-depth rect-height) ; carve out the corners
               (translate [(/ rect-width 2) (/ rect-depth 2) 0] (cylinder curve-radius rect-height))
               (translate [(/ rect-width -2) (/ rect-depth 2) 0] (cylinder curve-radius rect-height))
               (translate [(/ rect-width -2) (/ rect-depth -2) 0] (cylinder curve-radius rect-height))
               (translate [(/ rect-width 2) (/ rect-depth -2) 0] (cylinder curve-radius rect-height))
              )
   )
   (->> nut-hole                                       ; corner hole (calling that top right)
        (translate [(+ (/ rect-width 2) 4)
                    (+ (/ rect-depth 2) 4 3.225)
                    0]))
   (->> nut-hole                                       ; upper tine
        (translate [(+ (/ rect-width 2) 4 -205)        ; left/right down/up the tine
                    (+ (/ rect-depth 2) 4 5)           ; up and down
                    0]))
   (->> nut-hole                                       ; span hole (not on a tine)
        (translate [(+ (/ rect-width 2) 4  15)
                    (+ (/ rect-depth 2) 4 -250)
                    0]))                               ; use 345 here for a notch instead of a hole
   (->> nut-hole                                       ; lower tine
        (translate [(+ (/ rect-width 2) 4  -229.027)
                    (+ (/ rect-depth 2) 4 -395)
                    0]))
))


(def shift-down-height
  (let [x (* (/ total-width 2) (Math/tan slope))]
    (* (- rect-height x)
       (Math/cos slope)
    )
  )
)

(def flush-top
  (->> (union main-base (translate [0 0 -100] main-base))
       (rotate (- slope) [0 1 0])
       (translate [0 0 (- shift-down-height)])
  )
)

(def tent
  (scale [(/ 25.4 90) (/ 25.4 90) (/ 25.4 90)]
         (difference
          flush-top
          (translate [0 0 -250] (cube 700 700 500))                               ; the smaller box carved into the in the large box
          (translate [-375 0 -0] (cube 700 700 500))                              ; cut half of the top of the box off
          (translate [0 0 100] (cube (* 0.9 rect-width) (* 0.95 rect-depth) 300)) ; the large box
         )
  )
)

(spit "resources/infinity-wip.scad"
      (write-scad
       (union
        tent
        ;(->> tent (mirror [1 0 0]) (translate [50 (- 20) 0])) ; the other one.
       )
      )
)
