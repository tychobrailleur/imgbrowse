(ns imgbrowse.core
     (:use seesaw.core seesaw.graphics)
     (:import java.io.File java.awt.event.KeyEvent java.awt.Toolkit)
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

;; the-images store the list of images to be displayed.
(def the-images (atom ()))
;; the-image is the currently displayed image.
(def the-image (atom ""))

(defn update-image [image] (.getAbsolutePath (rand-nth @the-images)))

(defn read-image-file
  "Reads an image from a file and returns a BufferedImage."
  [img]
  (javax.imageio.ImageIO/read (File. img)))

(defn draw-image [c g img frame_width frame_height]
  (let [image (read-image-file img)
        image_width (.getWidth image nil)
        image_height (.getHeight image nil)
        image_x (if (>= image_width frame_width) 0 (/ (- frame_width image_width) 2))
        image_y (if (>= image_height frame_height) 0 (/ (- frame_height image_height) 2))
        width (if (>= image_width frame_width) frame_width image_width)
        height (if (>= image_height frame_height) frame_height image_height)]
  (.drawImage g image image_x image_y width height nil)))

(defn image-panel []
  (border-panel
    :id :imagepanel
    :border 2
    :center (canvas :id :image
                    :paint #(draw-image %1 %2 @the-image (.getWidth %1) (.getHeight %1)))))

(defn display-fullscreen [f]
  (let [screen-size (.getScreenSize (Toolkit/getDefaultToolkit))]
  (.setSize f screen-size)))

(defn add-behaviour [f]
  (let [c (select f [:#image])]
    (listen c :mouse-clicked (fn [e] (repaint! c)))
    (listen f :key-pressed (fn [e] (let [key-pressed (.getKeyCode e)] (cond
                                                                       (= key-pressed (KeyEvent/VK_F)) (display-fullscreen f)
                                                                       :else (swap! the-image update-image)
                                                                       )
                                        (repaint! c)))))
 f)

(defn -main [& args]
  (swap! the-images (fn [_ args] (list-images (first args))) args)
  (swap! the-image update-image)
  (invoke-later
    (->
      (frame
        :title "ImgBrowse"
        :size  [600 :by 800]
        :on-close :exit
        :content (image-panel))
      add-behaviour
      show!)))
