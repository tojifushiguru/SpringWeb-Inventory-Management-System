
# Pull the MySQL image
docker pull mysql

# Run the MySQL image
docker run -d -p 3308:3306 --name=mysqlcontainer --env="MYSQL_ROOT_PASSWORD=root" --env="MYSQL_PASSWORD=root" --env="MYSQL_DATABASE=java_spring_boot_db" mysql

# Build the image 
docker build -t springimage .

# Run the springimage 
docker run -t --link mysqlcontainer:mysql -p 10000:10000 springimage

# Link 
http://localhost:10000/
