curl -vvv -o exec2new --trace-ascii exec2new-log.txt -X POST --data-binary @exec2.txt -H "Content-Type: multipart/form-data; boundary=---------------------------18467633426500" "http://localhost:8071/api/execRequest?command=svcRegSignStamp"