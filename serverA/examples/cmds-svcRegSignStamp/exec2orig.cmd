curl -vvv -o exec2orig --trace-ascii exec2orig-log.txt -X POST --data-binary @exec2.txt -H "Content-Type: multipart/form-data; boundary=---------------------------18467633426500" "http://localhost:8070/ext-sedsvc/upload-m/?command=svcRegSignStamp"