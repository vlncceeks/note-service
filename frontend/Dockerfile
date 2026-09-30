FROM nginx:alpine
COPY public /usr/share/nginx/html
COPY default.conf.template /etc/nginx/templates/default.conf.template
ENV BACKEND_URL=http://backend:8080
