#!/bin/sh
# cacerts.sh
openssl s_client -showcerts -connect $1:443 </dev/null 2>/dev/null | openssl x509 -outform PEM > /tmp/$1.pem
/home/bruno/.jdks/corretto-19.0.2/bin/keytool -import -trustcacerts -file /tmp/$1.pem -alias $1 -keystore /home/bruno/.jdks/corretto-19.0.2/lib/security/cacerts
rm /tmp/$1.pem
