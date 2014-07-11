BotA-Patcher
============

BotA Patcher is a file patcher developed for the BotA Patch Server. It uses hashes to keep all your files up to date, download missing files and much more.
It stores all the download data in a /files folder within it's execution directory.

An example config.properties file:

```
#BotA Game Patcher Properties File
#Fri Jul 11 19:01:24 CEST 2014

# BotA Patch server protocol (currently only HTTP available)
server.protocol=http
# BotA Patch server port (standard is 80, configure it for your own purpose)
server.port=80
# The host name of the patch server (for example www.mysupergame.com)
server.host=localhost
```

I hope you have fun with the patcher! When you need a new feature, just request it or fork the repository and do it yourself.
