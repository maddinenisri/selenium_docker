FROM node:carbon
WORKDIR ./
COPY ./package.json ./package-lock.json ./main.js ./
RUN npm install
CMD [ "npm", "start" ]