(ns auth.core
  (:require
    [compojure.core :refer [ANY GET POST defroutes routes context]]
    [auth.html :refer [login-form register-form]]
    [auth.login :refer [do-login]]
    [auth.logout :refer [logout]]
    [auth.reset :refer [reset-form get-code set-pass]]
    [auth.register :refer [register]]))


(defroutes auth-routes
  (GET  "/login"  [] login-form)
  (POST "/login"  [] do-login)
  (POST "/logout" [] logout)

  (GET  "/register" [] register-form)
  (POST "/register" [] register)

  (context "/reset" []
    (routes
      (GET  "/"          [] reset-form)
      (POST "/get-code"  [] get-code)
      (POST "/set-pass"  [] set-pass))))
