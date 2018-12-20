(ns protok.forms.validators
  (:require [clojure.string :as str]
            [forms.validator :as v]
            [oops.core :refer [ocall]]))

(def countries
  ["United States" "United Kingdom" "Afghanistan" "Albania" "Algeria" "American Samoa" "Andorra" "Angola" "Anguilla" "Antarctica" "Antigua and Barbuda" "Argentina" "Armenia" "Aruba" "Australia" "Austria" "Azerbaijan" "Bahamas" "Bahrain" "Bangladesh" "Barbados" "Belarus" "Belgium" "Belize" "Benin" "Bermuda" "Bhutan" "Bolivia" "Bosnia and Herzegovina" "Botswana" "Bouvet Island" "Brazil" "British Indian Ocean Territory" "Brunei Darussalam" "Bulgaria" "Burkina Faso" "Burundi" "Cambodia" "Cameroon" "Canada" "Cape Verde" "Cayman Islands" "Central African Republic" "Chad" "Chile" "China" "Christmas Island" "Cocos (Keeling) Islands" "Colombia" "Comoros" "Congo" "Congo, The Democratic Republic of The" "Cook Islands" "Costa Rica" "Cote D'ivoire" "Croatia" "Cuba" "Cyprus" "Czech Republic" "Denmark" "Djibouti" "Dominica" "Dominican Republic" "Ecuador" "Egypt" "El Salvador" "Equatorial Guinea" "Eritrea" "Estonia" "Ethiopia" "Falkland Islands (Malvinas)" "Faroe Islands" "Fiji" "Finland" "France" "French Guiana" "French Polynesia" "French Southern Territories" "Gabon" "Gambia" "Georgia" "Germany" "Ghana" "Gibraltar" "Greece" "Greenland" "Grenada" "Guadeloupe" "Guam" "Guatemala" "Guinea" "Guinea-bissau" "Guyana" "Haiti" "Heard Island and Mcdonald Islands" "Holy See (Vatican City State)" "Honduras" "Hong Kong" "Hungary" "Iceland" "India" "Indonesia" "Iran, Islamic Republic of" "Iraq" "Ireland" "Israel" "Italy" "Jamaica" "Japan" "Jordan" "Kazakhstan" "Kenya" "Kiribati" "Korea, Democratic People's Republic of" "Korea, Republic of" "Kuwait" "Kyrgyzstan" "Lao People's Democratic Republic" "Latvia" "Lebanon" "Lesotho" "Liberia" "Libyan Arab Jamahiriya" "Liechtenstein" "Lithuania" "Luxembourg" "Macao" "Macedonia, The Former Yugoslav Republic of" "Madagascar" "Malawi" "Malaysia" "Maldives" "Mali" "Malta" "Marshall Islands" "Martinique" "Mauritania" "Mauritius" "Mayotte" "Mexico" "Micronesia, Federated States of" "Moldova, Republic of" "Monaco" "Mongolia" "Montenegro" "Montserrat" "Morocco" "Mozambique" "Myanmar" "Namibia" "Nauru" "Nepal" "Netherlands" "Netherlands Antilles" "New Caledonia" "New Zealand" "Nicaragua" "Niger" "Nigeria" "Niue" "Norfolk Island" "Northern Mariana Islands" "Norway" "Oman" "Pakistan" "Palau" "Palestinian Territory, Occupied" "Panama" "Papua New Guinea" "Paraguay" "Peru" "Philippines" "Pitcairn" "Poland" "Portugal" "Puerto Rico" "Qatar" "Reunion" "Romania" "Russian Federation" "Rwanda" "Saint Helena" "Saint Kitts and Nevis" "Saint Lucia" "Saint Pierre and Miquelon" "Saint Vincent and The Grenadines" "Samoa" "San Marino" "Sao Tome and Principe" "Saudi Arabia" "Senegal" "Serbia" "Seychelles" "Sierra Leone" "Singapore" "Slovakia" "Slovenia" "Solomon Islands" "Somalia" "South Africa" "South Georgia and The South Sandwich Islands" "Spain" "Sri Lanka" "Sudan" "Suriname" "Svalbard and Jan Mayen" "Swaziland" "Sweden" "Switzerland" "Syrian Arab Republic" "Taiwan, Province of China" "Tajikistan" "Tanzania, United Republic of" "Thailand" "Timor-leste" "Togo" "Tokelau" "Tonga" "Trinidad and Tobago" "Tunisia" "Turkey" "Turkmenistan" "Turks and Caicos Islands" "Tuvalu" "Uganda" "Ukraine" "United Arab Emirates" "United Kingdom" "United States" "United States Minor Outlying Islands" "Uruguay" "Uzbekistan" "Vanuatu" "Venezuela" "Viet Nam" "Virgin Islands, British" "Virgin Islands, U.S." "Wallis and Futuna" "Western Sahara" "Yemen" "Zambia" "Zimbabwe"])

(def country-set (set countries))

(def email-regex #"^([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x22([^\x0d\x22\x5c\x80-\xff]|\x5c[\x00-\x7f])*\x22)(\x2e([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x22([^\x0d\x22\x5c\x80-\xff]|\x5c[\x00-\x7f])*\x22))*\x40([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x5b([^\x0d\x5b-\x5d\x80-\xff]|\x5c[\x00-\x7f])*\x5d)(\x2e([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x5b([^\x0d\x5b-\x5d\x80-\xff]|\x5c[\x00-\x7f])*\x5d))*$")

(def url-regex #"https?://(www\.)?[-a-zA-Z0-9@:%._\+~#=]{2,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\+.~#?&//=]*)")

(def us-states
  #{"AL", "AK", "AS", "AZ", "AR", "CA", "CO", "CT", "DE", "DC", "FL", "GA", "GU", "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD", "MH", "MA", "MI", "FM", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "MP", "OH", "OK", "OR", "PW", "PA", "PR", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "VA", "VI", "WA", "WV", "WI", "WY"})

(def phone-regex
  #"^[(]{0,1}[0-9]{3}[)]{0,1}[-\s\.]{0,1}[0-9]{3}[-\s\.]{0,1}[0-9]{4}$")

(defn phone? [v _ _]
  (not (nil? (re-matches phone-regex (str v)))))

(defn zero-count? [v]
  (if (satisfies? ICounted v)
    (zero? (count v))
    false))

(defn not-empty? [v _ _]
  (cond
    (nil? v) false
    (= "" v) false
    (zero-count? v) false
    :else true))

(defn url? [v _ _]
  (not (nil? (re-matches url-regex (str v)))))

(defn email? [v _ _]
  (if (or (nil? v) (empty? v))
    true
    (re-matches email-regex (str v))))

(defn edu-email? [v _ _]
  (if (nil? v)
    true
    (str/ends-with? (str/trim v) ".edu")))

(defn number0>100? [v _ _]
  (if (not (not-empty? v nil nil))
    true
    (let [n (js/parseFloat v 10)]
      (and (< 0 n) (>= 100 n)))))

(defn bool? [v _ _]
  (if (nil? v)
    true
    (or (= true v) (= false v))))

(defn numeric? [v _ _]
  (if (nil? v)
    true
    (re-matches #"^\d+$" v)))

(defn ok-password? [v _ _]
  (if (seq v)
    (< 7 (count v))
    true))

(defn valid-us-state? [v _ _]
  (if (seq v)
    (contains? us-states (str/upper-case v))
    true))

(defn valid-country? [v _ _]
  (if (seq v)
    (contains? country-set v)
    true))

(defn valid-cvv? [v _ _]
  (if (seq v)
    (or (= 3 (count v))
        (= 4 (count v)))
    true))


(defn valid-zipcode? [v _ _]
  (if (seq v)
    (not (nil? (re-matches #"(^\d{5}$)|(^\d{5}-\d{4}$)" (str v))))
    true))

(defn password-confirmation [_ data _]
  (let [pass (:password data)
        pass-confirmation (:password2 data)]
    (if (some nil? [pass pass-confirmation])
      true
      (= pass pass-confirmation))))

(defn cardholder-name [_ data _]
  (let [nonce (:paypal-nonce data)
        cardholder-name (:cardholder-name data)]
    (if nonce true 
        (not-empty? cardholder-name _ _))))

(def default-validations
  {:not-empty          {:message   "Value can't be empty"
                        :validator not-empty?}
   :wrong-access-token {:message   "Wrong access-token"
                        :validator (fn [_ _ _] true)}
   :bool               {:message   "Value must be true or false"
                        :validator bool?}
   :url                {:message   "Value is not a valid URL"
                        :validator url?}
   :email              {:message   "Value is not a valid email"
                        :validator email?}
   :edu-email          {:message   "Not a valid .edu email"
                        :validator edu-email?}
   :email-confirmation {:message   "Email doesn't match email confirmation"
                        :validator (fn [_ data _]
                                     (let [email              (:email data)
                                           email-confirmation (:email-confirmation data)]
                                       (if (some nil? [email email-confirmation])
                                         true
                                         (= email email-confirmation))))}

   :us-state {:message   "Please select state"
              :validator (fn [v data _]
                           (let [country (:country data)]
                             (not (and (= "United States" country)
                                       (or (= "State *" v) (nil? v) (empty? v))))))}

   :password-confirmation {:message   "Passwords don't match"
                           :validator password-confirmation}
   :ok-password           {:message   "Password must have at least 8 characters"
                           :validator ok-password?}
   :numeric               {:message   "Value is not a number"
                           :validator numeric?}
   :phone                 {:message   "Value is not a valid phone"
                           :validator phone?}
   :valid-us-state        {:message   "Not a valid US state"
                           :validator valid-us-state?}
   :valid-zipcode         {:message   "Not a valid Zipcode"
                           :validator valid-zipcode?}
   :valid-country         {:message   "Not a valid Country"
                           :validator valid-country?}
   :valid-cardholder      {:message   "Cardholder name must not be empty"
                           :validator cardholder-name}

   :0>                     {:message   "Must be bigger than zero"
                            :validator (fn [v _ _]
                                         (if (not (not-empty? v nil nil))
                                           true
                                           (< 0 (js/parseFloat v))))}
   :0>100                  {:message   "Must be between 0 and 100"
                            :validator number0>100?}
   :future-date            {:message   "Date must be in the future"
                            :validator (fn [v _ _]
                                         (let [current-timestamp (ocall js/Date "now")]
                                           (if (not (not-empty? v nil nil))
                                             true
                                             (< current-timestamp (ocall v "format" "x")))))}
   :future-date-from-start {:message   "Date must after the start date"
                            :validator (fn [_ data _]
                                         (let [start (get-in data [:startDatetime])
                                               end   (get-in data [:endDatetime])]
                                           (if (or (not (not-empty? start nil nil))
                                                   (not (not-empty? end nil nil)))
                                             true
                                             (< (ocall start "format" "x") (ocall end "format" "x")))))}})

(def validations$ (atom default-validations))

(defn register-validation! [key validator]
  (swap! validations$ assoc key validator))

(defn get-validator-message [validation-key]
  (or (get-in @validations$ [validation-key :message])
      "Value failed validation."))

(defn to-validator
  "Helper function that extracts the validator definitions."
  [config]
  (v/validator
   (reduce-kv (fn [m attr v]
                (assoc m attr
                       (map (fn [k] [k (get-in @validations$ [k :validator])]) v))) {} config)))

