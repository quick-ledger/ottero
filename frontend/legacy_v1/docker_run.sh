git pull
docker build -t ottero-react .
docker run -d -p 80:3000 ottero-react