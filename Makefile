.PHONY : deploy deploy-production netlify-deploy netlify-deploy-production build-prod-cljs build-prod-js-deps build-dev-js-deps run-dev-server develop

build-dev-js-deps :
	yarn
	NODE_ENV=development yarn parcel build src/js/index.js -d dist --no-minify

build-prod-js-deps :
	yarn
	yarn parcel build src/js/index.js -d dist  

run-dev-server : 
	lein figwheel dev

build-prod-cljs :
	lein clean
	lein cljsbuild once min

develop : build-dev-js-deps run-dev-server

netlify-deploy :
	netlify deploy --dir resources/public

netlify-deploy-production :
	netlify deploy --prod --dir resources/public

deploy : build-prod-js-deps build-prod-cljs netlify-deploy

deploy-production : build-prod-js-deps build-prod-cljs netlify-deploy-production
