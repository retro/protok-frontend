.PHONY : deploy deploy-production netlify-deploy netlify-deploy-production build-prod-cljs

build-prod-cljs :
	lein clean
	lein fig:min

netlify-deploy :
	netlify deploy --dir resources/public

netlify-deploy-production :
	netlify deploy --prod --dir resources/public

deploy : build-prod-cljs netlify-deploy

deploy-production : build-prod-cljs netlify-deploy-production