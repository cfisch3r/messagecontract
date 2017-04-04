Prerequisites
==========

A DOCKER_HOST environment variable has to be set. On Mac this is:
unix:///var/run/docker.sock

After a while, this stops working on my Mac. Than I stop docker, remove the /var/run/docker.sock file and restart docker.
This fixes the problem for me.