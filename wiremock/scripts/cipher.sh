# OpenSSL requires the port number.
# In order to test it use: ./cipher.sh jsonplaceholder.typicode.com:443
# https://stackoverflow.com/questions/6353849/received-fatal-alert-handshake-failure-through-sslhandshakeexception
SERVER=$1
DELAY=1
ciphers=$(openssl ciphers 'ALL:eNULL' | sed -e 's/:/ /g')

echo Obtaining cipher list from $(openssl version).

for cipher in ${ciphers[@]}
do
  echo -n Testing $cipher...
  # -ssl3, -tls1, -tls1_1, -tls1_2, -tls1_3, -no_ssl3, -no_tls1, -no_tls1_1, -no_tls1_2, -no_tls1_3
  result=$(echo -n | openssl s_client -tls1_3 -cipher "$cipher" -connect $SERVER 2>&1)
  if [[ "$result" =~ ":error:" ]] ; then
    error=$(echo -n $result | cut -d':' -f6)
    echo NO \($error\)
  else
    if [[ "$result" =~ "Cipher is ${cipher}" || "$result" =~ "Cipher    :" ]] ; then
      echo YES
    else
      echo UNKNOWN RESPONSE
      echo $result
    fi
  fi
  sleep $DELAY
done