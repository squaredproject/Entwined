ip=105
while true; do
	echo -n Press enter to flash 10.0.0.$ip
	read foobar
	curl 'http://10.0.0.100/00.html' -H 'Origin: http://10.0.0.100' -H 'Accept-Encoding: gzip,deflate,sdch' -H 'Accept-Language: en-US,en;q=0.8' -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36' -H 'Content-Type: application/x-www-form-urlencoded' -H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8' -H 'Cache-Control: max-age=0' -H 'Referer: http://10.0.0.100/00.html' -H 'Connection: keep-alive' --data 'I000=10&I001=0&I002=0&I003='"$ip"'&I004=255&I005=255&I006=255&I007=0&I008=10&I009=0&I010=0&I011=1&I012=0&I013=1&I014=0&I015=6&I016=0&I017=0&I018=6&I019=18&I020=0&I021=6&I022=36&I023=0&I024=6&I025=54&I026=0&I027=6&I028=72&I029=0&I030=12&I031=90&I032=0&I033=6&I034=126&I035=0&I036=6&I037=144&I038=0&I039=6&I040=162&I041=0&I042=6&I043=180&I044=0&I045=12&I046=198&I047=0&I048=6&I049=234&I050=0&I051=12&I052=252&I053=0&I054=6&I055=288&I056=0&I057=6&I058=306&I059=0&I060=6&I061=324&op=Save' --compressed
	ip=$(( ip + 1 ))
done