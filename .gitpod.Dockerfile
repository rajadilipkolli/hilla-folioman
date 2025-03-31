FROM gitpod/workspace-full

USER root

RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh \
             && sdk install java 21.0.6-graal \
             && sdk default java 21.0.6-graal"

             
