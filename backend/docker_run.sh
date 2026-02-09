# todo put maven setting in the app
#ghcr is not free for our images sizes.
mvn clean package -DskipTests
docker build -t ottero-service .

docker run -it -p 8080:8080   -v ~/.aws:/root/.aws:ro -e AWS_PROFILE=ql ql-service