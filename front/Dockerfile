FROM node:18-alpine
COPY package.json package.json
COPY public public
COPY src src
RUN npm install
CMD [ "npm", "start" ]