#!/bin/bash

echo "=========================================="
echo "|        Script to add service           |"
echo "=========================================="

read -p "| username....: " name
password=Aqa123456!
email=smoketester+$name@wire.com

echo $email

# Authenticate to Wire
resp=$(curl -s -XPOST "https://staging-nginz-https.zinfra.io/login" \
    -H 'Content-Type: application/json' \
    -d '{"email": "'"$email"'", "password": "'"$password"'"}')

#echo $resp

token=$(echo "$resp" | jq -r ".access_token")
if [ token == "null" ]; then
	echo " "
	echo " "
	echo "Authentication error!!"
	echo " "
	exit 1
fi

provider=d64af9ae-e0c5-4ce6-b38a-02fd9363b54c
service=d554c310-8237-4f85-b3cc-b7ae5ec1e6cd

# function needed to bypass curl problems with shell vars
build_data_conv()
{
 cat <<EOF
{
"users": [],"name":"eAlarming"
}
EOF
}

#creating conv in wire
curl -s -XPOST "https://staging-nginz-https.zinfra.io/conversations" -H 'Content-Type: application/json' -H 'Authorization:Bearer '${token}'' -d "$(build_data_conv)" | jq -r '.id' > .conv

conv=$(cat .conv)

# function needed to bypass curl problems with shell vars
build_data_prov()
{
 cat <<EOF
{
"provider":"${provider}","service":"${service}"
}
EOF
}


# adding bot to the conv room
curl -s -XPOST 'https://staging-nginz-https.zinfra.io/conversations/'${conv}'/bots' -H 'Content-Type: application/json' -H 'Authorization:Bearer '${token}'' -d "$(build_data_prov)"

echo ""
echo "Done!"
