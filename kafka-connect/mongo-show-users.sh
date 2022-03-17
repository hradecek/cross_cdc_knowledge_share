#!/bin/bash
docker-compose exec mongo mongo cross_db --eval 'db.users.find()'

