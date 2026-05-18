FROM postgres:14-bullseye

# The docker build environment clock is behind the Debian security mirror
# release signatures ("not valid yet"). hypopg comes from pgdg only, so
# removing the security source is safe here.
RUN sed -i '/security/d' /etc/apt/sources.list \
 && apt-get update \
 && apt-get install -y postgresql-14-hypopg \
 && rm -rf /var/lib/apt/lists/*
