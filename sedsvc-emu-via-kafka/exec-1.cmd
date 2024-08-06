@rem product mode
call 0-make-jar.cmd
@IF %ERRORLEVEL% GTR 0 goto L_ERR_1
start java -Djava.io.tmpdir=tmpFolder -DSkipTests=true -jar target/sedsvc-emu-via-kafka-0.0.1-SNAPSHOT.jar
goto L_END

:L_ERR_1
echo compile error. need fixup
goto L_END

:L_END
