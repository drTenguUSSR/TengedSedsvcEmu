package mil.teng.q2024.sedsvc.emu.via.kafka.temp.common;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * no-product code
 *
 * @author DrTengu, 2024/07
 */
public class Temp7 {
    public static void main(String[] args) throws JsonProcessingException {
        xlog("Temp7/01");

        //xlog("text:" + text);
        xlog("end");
    }

    public static void xlog(String msg) {
        System.out.println(msg);
    }


}
