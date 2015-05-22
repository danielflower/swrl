(ns yswrl.test.fake_data
  (:require [yswrl.test.scaffolding :refer :all]
            [yswrl.swirls.swirls-repo :as swirl]
            [yswrl.auth.auth-repo :as user])
  )
(use 'korma.core)
(use 'korma.db)

(def books ["Babel-17",
            "The Bane of Yoto ",
            "Barefoot in the Head",
            "Barsoom series ",
            "Battle Angel Alita ",
            "Battlefield Earth",
            "The Beast Master ",
            "Becoming Alien",
            "The Bell-Tower ",
            "Berserker",
            "Between Planets ",
            "Beyond Apollo",
            "The Bikers series",
            "Big Planet series ",
            "namely, Big Planet and Showboat World ",
            "The Big Time ",
            "A Billion Days of Earth",
            "Black ",
            "Black Legion of Callisto",
            "Blast Off at Woomera ",
            "Blackstar",
            "The Blind Worm ",
            "Bloodchild and Other Stories",
            "Blood Music ",
            "The Blue Barbarians",
            "The Blue Man ",
            "The Blue World",
            "Borgel ",
            "Born With the Dead",
            "Borrowed Tides ",
            "Brave New World",
            "Brasyl "])

(def reviews ["Lorem ipsum dolor sit amet, non metus erat, nibh mattis, anim hendrerit. Sagittis leo sed, purus augue sed. In ac tellus, mattis non. Ut urna in, nisl odio dictum, velit et massa. Mi placerat, ac aliquam. Mollis vitae id, pulvinar integer, integer ipsum pretium.",
              "Nec eu. Nulla montes donec, morbi odio placerat, felis itaque morbi. Eros facilisi, hendrerit eget volutpat, sit cursus. Maecenas vivamus pharetra, sodales etiam sed. Mollis mollis leo, ac est, ante bibendum. Erat nec, pretium porta. "
              "Feugiat libero justo, id amet. Praesent vestibulum. Sociis risus auctor, nec dui donec. Eu id. Tincidunt ultrices, dictum sit, ultrices mauris vivamus.",
              "Venenatis vel sit, adipiscing etiam ante, in nullam. Cras neque cras, lectus lectus tincidunt. In dolor ornare, eu pede in. Pellentesque augue, neque mattis quis, mauris justo suscipit. In vel ante, porta duis. "
              "Proin vel. Pellentesque egestas. Consequat a placerat, bibendum sagittis, malesuada nec. Sed ac sapien. Viverra id mus, risus odio, et a. Ducimus id nec, quisque hac tempor, eu neque.",
              "Posuere ultrices sed. Felis consequat id, id massa. Duis aliquam. Metus ligula ut, leo nulla mollis, eget tellus. Nibh quisque sed, non praesent auctor, felis bibendum. Tempus et, duis condimentum, arcu porttitor pulvinar. "
              "Eros vitae, sed id. Porttitor sit ipsum. Velit proin sollicitudin, vitae porttitor, fringilla interdum ut. Ut libero, et dui mi. Vestibulum leo.",
              "Metus lobortis eget, erat ligula rhoncus. Sollicitudin dui. Ut egestas, nisl platea, in hendrerit. Id vel id. Luctus vel ipsum, non augue et. Nullam malesuada, pretium donec. "
              "Elit tellus, ut magna, nam lorem luctus. Sodales duis vel. Enim wisi vitae, interdum inceptos semper, in tristique. Est consectetuer. Sit ante et.",
              "Semper magna, eleifend risus class. Dui proin neque. Elit vitae, mus sem quisque. Et tortor at, malesuada potenti. Condimentum lectus, pede eget, vestibulum massa. Sit tellus, ultrices est ut. "
              "Euismod maecenas nonummy. A lorem ipsum, metus magna elit. Leo per nulla, suscipit quam tristique, eu arcu. Voluptate sed nonummy, ut maecenas, lorem porta ut. Iaculis amet rutrum.",
              "Dui donec. A at, nulla lacinia tristique. Ad sed sodales. Euismod est sit, sit justo ante. Donec aliquam velit, amet ac, consequat phasellus. Proin wisi pede. "
              "A id urna, curabitur vitae. Curabitur cum hac, vel diam, justo wisi wisi. Adipiscing bibendum eget, sed orci. Elementum sed consectetuer, ac massa. Et ac.",
              "Consectetuer sint, est harum, laoreet porta. Tellus condimentum vitae. Risus amet, dolor amet, proin ligula cum. Donec ante, dolor platea tellus, aliquam eros a. Suscipit per, tortor porta lobortis, hac tincidunt lacus. Suscipit arcu. "
              "Convallis congue. Id vitae, sed urna duis. Lorem faucibus in, iaculis vulputate, velit vitae. Tincidunt sagittis orci, bibendum enim tellus, praesent rhoncus nisl. Mus turpis, mi voluptas.",
              "Dolor malesuada, eu nullam. Nulla urna, non penatibus feugiat. Mi sed. Mollis eget vel, cras mauris. Euismod pellentesque platea. "
              "Amet id amet, rhoncus pede. Sed at sed, metus elementum imperdiet. Dictum eros tempor, non vel, aenean vehicula in. Sit porta et. A vivamus, neque elit a, risus placerat.",
              "Ligula vestibulum nulla. Risus sit purus, pellentesque ac ac. Sollicitudin ac, neque maecenas eros. Eu odio. Vestibulum ut, ullamcorper metus ipsum, massa nunc a. Nostrum cras, vivamus nisl, molestie recusandae. "
              "Consequat praesent, odio vitae placerat, at vivamus lectus. Aptent quam. Fringilla fugit, libero quis. Nec dui curabitur, sed vestibulum, amet amet elementum. Scelerisque maecenas nec.",
              "Sapien ac primis, quis nullam, rutrum semper itaque. Dolor dolor, pellentesque wisi proin, magna dis feugiat. Vel et. Beatae pellentesque id. Dolor posuere. Consectetuer euismod nec, at et. "
              "Nisl justo, malesuada fusce eleifend. Metus tempus, auctor varius. Consequat mi penatibus, dis nulla. Tortor integer elementum. Sapien molestie, quam suspendisse elit, torquent porttitor egestas. Laoreet sed ultricies, ipsum augue conubia.",
              "Tellus commodo, ullamcorper eu. Tellus odio, dolor quam, nascetur cursus. Elementum augue sed. Netus ligula. Vehicula volutpat accumsan, ipsum lorem commodo, id velit. Quisque nostra, donec vestibulum justo. "
              "Arcu amet. Mollis iaculis, in arcu. A feugiat, nunc lorem integer. Ullamcorper massa vulputate, dolor risus, ligula donec cursus. Curabitur lobortis sem. Nulla molestie, magna aliquam sapiente, nec elit.",
              "Volutpat velit convallis. In in sit. Eros nam sodales. Fusce sit et. Dolor purus. In egestas, hendrerit wisi tortor, est pede non. "
              "Ipsum etiam dui. Auctor est, turpis ligula, scelerisque faucibus blandit. Incididunt tempor suscipit, phasellus cras. Et senectus consectetuer, et ullamcorper. Tempor arcu sed, sed amet omnis, amet tellus.",
              "Lectus libero, nulla suspendisse. Pede urna. Dignissim sed, adipiscing dolor, vel auctor cum. Ullamcorper tellus, velit vestibulum vulputate. Nisl convallis aliquam, dui rutrum pretium. Nibh in. "
              "Facilisi ultricies. Fringilla dui, blandit vel mauris. Fringilla eleifend neque. Ultrices sed, eget eu, mauris molestie eget. Fusce rhoncus, quis nullam, ipsum convallis lobortis. Nibh congue aliquet, eget aliquam pellentesque, ultricies integer.",
              "Bibendum tellus ut, in elementum. Praesent eget. Libero vestibulum. Viverra vehicula eleifend. Dictum tristique suspendisse. "
              "Erat justo donec, eget integer ipsum, erat at gravida. Erat aliquet, dolor interdum rutrum. Laoreet auctor ipsum. Dictum ad, varius viverra vivamus, suspendisse suspendisse. Aliquam hac, tellus duis, tincidunt augue in. Metus enim, mauris pede erat, vestibulum nisl.",
              "Lorem ante lorem. Nullam voluptatem, dapibus diam in. Nisl justo. Phasellus aliquam dui. Mus varius, dolorum interdum, maecenas nostra."])

(defn createUsers []
  (user/create-user  "Hector" "a@a.com" "pass")
  (user/create-user  "AdwinIssitrator" "b@a.com" "pass")
  (user/create-user  "Mike" "c@a.com" "pass")
  (user/create-user  "Dan" "d@a.com" "pass")
  (user/create-user  "Sam" "e@a.com" "pass")
  (user/create-user  "DanNorth" "f@a.com" "pass")
  (user/create-user  "Jez" "g@a.com" "pass")
  (user/create-user  "Anonymous123" "h@a.com" "pass")
  (user/create-user  "Joshwa" "i@a.com" "pass")
  (user/create-user  "ObiWan" "j@a.com" "pass")
  )

(defn createData []
  (for [x (range 30)]
    (create-swirl "book" (+ 1 (rand-int 9)) (nth books (rand-int (count books))) (nth reviews (rand-int (count reviews))) " " , {})
    )
  )