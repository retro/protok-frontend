(ns protok.settings)

(def gql-endpoint
  (if js/goog.DEBUG
    "http://localhost:3000/graphql"
    "https://protok-staging.herokuapp.com/graphql"))

(def jwt-ls-name "protok-jwt")

(def elk-worker-path "./js/elk-worker.min.js");
