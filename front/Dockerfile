FROM node:18-alpine
COPY package.json package.json
COPY public public
COPY src src
RUN npm install
EXPOSE 3000
CMD [ "npm", "start" ]