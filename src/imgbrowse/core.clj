(ns imgbrowse.core
     (:use seesaw.core seesaw.graphics)
     (:import org.pushingpixels.substance.api.SubstanceLookAndFeel java.io.File)
  (:gen-class))

(native!)

(defn picture? [file]
  (let [filename (.toUpperCase (.getName file))]
    (or (.endsWith filename "JPG")
        (.endsWith filename "PNG")
        (.endsWith filename "GIF"))))

(defn list-images
  "Lists all the images in a folder."
  [dir]
  (filter picture? (file-seq (File. dir))))

(def the-images (list-images (System/getProperty "user.home")))
(def the-image (atom (.getAbsolutePath (rand-nth the-images))))
(defn update-image [image] (.getAbsolutePath (rand-nth the-images)))

(defn read-image-file 
  "Reads an image from a file and returns a BufferedImage."
  [img]
  (javax.imageio.ImageIO/read (File. img)))

(defn draw-image [c g img x y]
  (let [image (read-image-file img)
        image_width (.getWidth image nil)
        image_height (.getHeight image nil)
        image_x (if (>= image_width x) 0 (/ (- x image_width) 2))
        image_y (if (>= image_height y) 0 (/ (- y image_height) 2))
        width (if (>= image_width x) x image_width)
        height (if (>= image_height y) y image_height)]
  (.drawImage g image image_x image_y width height nil)))

(defn image-panel []
  (border-panel 
    :id :imagepanel
    :border 2
    :center (canvas :id :image
                    :paint #(draw-image %1 %2 @the-image (.getWidth %1) (.getHeight %1)))))

(defn add-behaviour [f]
  (let [c (select f [:#image])]
    (listen c :mouse-clicked (fn [e] (repaint! c)))
    (listen f :key-pressed (fn [e] (swap! the-image update-image) 
                               (repaint! c))))
 f)

(defn -main [& args]
  (invoke-later
    (->
      (frame
        :title "ImgBrowse"
        :size  [600 :by 800]
        :on-close :exit
        :content (image-panel))
      add-behaviour
      show!)))
