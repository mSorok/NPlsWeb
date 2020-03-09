# NP-likeness scorer - Web application

Author: Maria Sorokina, maria.sorokina@uni-jena.de

Last modified: 06.03.2020


This README describes the steps on how to setup and run a local instance of the NP-likeness scorer web application.

### Project tree
```
NPlsWeb
├── archive
├── docker-compose.yml
├── Dockerfile
├── molimg
├── mvnw
├── mvnw.cmd
├── mysql
├── NPlsWeb.iml
├── pom.xml
├── README
├── src
├── target
├── upload-dir
└── volume
```

### Prior of running
- verify that the file NPlikenessDB.sql is in the "mysql" folder (can be downloaded here: https://zenodo.org/record/2652356)
- compile the project with Maven by running: "mvnv clean package" from the project root
- verify that there are ".ser" files in the "archive" folder
- in the docker-compose.yml remplace "VIRTUAL_HOST: nplsscorer.cheminf.uni-jena.de" by "VIRTUAL_HOST: yourwebsite.de" or remove this part to run the web application locally (don't forget to cite us!)
- in the docker-compose.yml remove "LETSENCRYPT_HOST" and "LETSENCRYPT_EMAIL" unless you want to have your NPLSweb instance to be certified by Let's Encrypt.
- in the docker-compose.yml remplace the MYSQL_ROOT_PASSWORD if necessary 

### nginx
The project is build to run with nginx as server and reverse-proxy on a Docker "nginx-network". For more details see for example here: https://medium.com/@francoisromain/host-multiple-websites-with-https-inside-docker-containers-on-a-single-server-18467484ab95



### Project execution`

```
sudo docker-compose build
sudo docker-compose up -d
sudo docker exec -it mysql_npls bash
mysql -uroot -proot1234 NPLikenessDB < /mysqldata/NPLikenessDB.sql
exit
```

After this, the web application will run either on the URL you specified in the docker-compose.yml either on localhost:8090


### Source code
The unarchived source code for this project is available at https://github.com/mSorok/NPlsWeb and can be recompiled as a maven project.
Here can be 
- modified the MySQL instance the web application connects on (in the application.properties file)
- modified the maximum number of molecules per submitted file via the web interface (currently max 200 molecules - to modify in the SDF and SMILES readers)
- if the plots need to be recomputed (due to changes in the database): in the NPlsWebController.java change private boolean newPlot = false to true (when the plots recomputed, don't forgive to change the newPlot back to false!).

