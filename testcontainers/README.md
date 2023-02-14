# General Notes

## Commands

Before running the application you should go to the folder `integration-tests/testcontainers/src/docker` and run the command below to start the PostgreSQL DB container:  
<code>docker-compose up -d</code>  

You also can use the commands bellow to list containers for the given docker compose configuration and to ensure the containers have stopped:  
<code>docker-compose ps</code>  
<code>docker-compose down</code>

Before running the integration tests log into the Docker Hub:  
<code>docker login</code>