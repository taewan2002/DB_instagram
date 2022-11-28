package chatting;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.*;
import java.util.concurrent.*;


public class file_client{


    DataOutputStream dataOutput = null;
    OutputStream os=null;
    Socket socket = null;
    File file = null;        //파일에 정보를 얻기위한 File 클래스
    PrintWriter pw  = null;
    DataInputStream dataInput = null;
    BufferedOutputStream bufferedOutput = null; //output 속도 향상을 위한 BufferedOutputStream

    String filetype;

    chatting_client A=null;

    String roomnumber;

    int type;

    String time;

    public String filename;


    public file_client(Socket socket, String fileName, String roomnumber, String time,int type,chatting_client A)
    {
        this.socket=socket;
        this.filename=fileName;
        this.roomnumber=roomnumber;
        this.time=time;
        this.type=type;
        this.A=A;
        sending tmp =new sending();
        tmp.run();
    }

    public class sending implements Runnable{
        public void run()
    {
        Socket socket=null;
        filechoose choice=new filechoose();
        String filename=choice.jFileChooserUtil();

        try{
        socket=new Socket("swiftsjh.tplinkdns.com",25589); //서버에 접속
        }catch(IOException e){
        System.out.println("It seems like There is no response from Server");
        }
        if(socket!=null){
        try{

        protocol time=new protocol();
        time.setTime();
        String name_send1=time.getTime();

        String filetype=filename.substring(filename.lastIndexOf("."));
        os=socket.getOutputStream();
        InputStream is=socket.getInputStream();
        pw=new PrintWriter(os);
        pw.println(Integer.toString(type)); //타입
        pw.flush();
        pw.println(roomnumber); //1방넘버
        pw.flush();
        System.out.println(time);
        pw.println(name_send1); //2파일이름
        pw.flush();
        pw.println(filetype); //3파일타입
        pw.flush();

        if(type==2){
        BufferedReader br=new BufferedReader(new InputStreamReader(is));
        String file_size=br.readLine();
        System.out.println("파일크기:"+file_size);
        byte[]buf=new byte[104857600];      //100MB 단위로 파일을 쓰기 위한 byte타입 배열
        FileOutputStream fileOutput=new FileOutputStream("chatting_data/"+roomnumber+"/"+filename,false);
        BufferedInputStream bufferdInput=new BufferedInputStream(dataInput);
        int totalSize=Integer.parseInt(file_size);
        int i=0;  //buf 배열 인덱스용 변수
        //전송받은 파일 사이즈가 100MB 보다 크다면 100MB 단위로 배열에 저장 후 파일에 write 하고
        //파일에 write한 100MB만큼을  파일 사이즈에서 제외하는 while문!!!
        while(totalSize>104857600){
        while(i< 104857600){
        buf[i]=(byte)bufferdInput.read();
        i++;    //배열인덱스 이동
        }//while(i < 104857600)문
        totalSize-=104857600;  //파일사이즈 - 100MB
        i=0;                   //배열 인덱스 초기화
        fileOutput.write(buf);   //파일에 write
        }//while(totalSize > 104857600)문

        //100MB보다 같거나 작은 남은 사이즈 혹은 원래의 사이즈가 100MB 보다 작을 시 if문 내용이 실행 되어
        //파일을 write 함
        if(totalSize<=104857600){
        i=0;                     //배열 인덱스 초기화
        buf=new byte[totalSize]; //100MB보다 같거나 작으므로 totalSize로 배열크기 다시 생성
        while(i<totalSize){
        buf[i]=(byte)bufferdInput.read();
        i++;      //배열인덱스 이동
        }//while문
        fileOutput.write(buf);  //파일에 write
        }//if문
        fileOutput.flush();
        System.out.println("file receive complete");
        socket.close();
        }


        // 파일 전송
        file=new File(filename);
        pw.println((int)file.length()); //4파일크기
        pw.flush();


        A.send_messege(4,roomnumber,A.user_id,"파일 전송 완료",time.getTime(),true,name_send1+filetype);

        os=socket.getOutputStream();
        dataOutput=new DataOutputStream(os); //output 스크림 생성
        // dataOutput.writeInt(132451235);(int) file.length());  //수신측에 전송파일 사이즈 전달
        // dataOutput.flush();

        System.out.println((int)file.length()); // 송신 파일 사이즈 콘솔출력
        System.out.println("filetype:"+filetype);

        int totalSize=(int)file.length();
        int totalSize2=(int)file.length(); // 아래에서 파일 전송시 totalsize 변수 크기가 변해서 원래 파일 크기 기억용 변수 만듬
        byte[]bytes=new byte[104857600];  //100MB 저장할 바이트 배열
        dataInput=new DataInputStream(new FileInputStream(filename));
        bufferedOutput=new BufferedOutputStream(dataOutput);
        int i=0;     //buf 배열 인덱스용 변수
        int progress=0; //몇 바이트가 전송됬는지 표시하는 변수

        //전송받은 파일 사이즈가 100MB 보다 크다면 100MB 단위로 배열에 저장 후 소켓 버퍼에 write 하고
        //소켓 버퍼에 write한 100MB만큼을  파일 사이즈에서 제외하는 while문!!!
        while(totalSize>104857600){
        while(i< 104857600){
        bytes[i]=(byte)dataInput.read();
        i++;
        if(progress/(float)totalSize2*100%0.5==0){
        System.out.println("전송 진행률: "+String.format("%.1f",progress/(float)totalSize2*100)+"%");
        }
        progress++;
        }//while(i < 104857600)문
        totalSize-=104857600;   //파일사이즈 - 100MB
        i=0;                    //배열 인덱스 초기화
        bufferedOutput.write(bytes);   //소켓 버퍼에 write
        }//while(totalSize > 104857600)문

        //100MB보다 같거나 작은 남은 사이즈 혹은 원래의 사이즈가 100MB 보다 작을 시 if문 내용이 실행 되어
        //소켓 버퍼에 write 함
        if(totalSize<=104857600){
        i=0;        //배열 인덱스 초기화
        bytes=new byte[totalSize];
        while(i<totalSize){
        bytes[i]=(byte)dataInput.read();
        i++;           //배열인덱스 이동
        if(progress/(float)totalSize2*100%0.5==0){
        System.out.println("전송 진행률: "+String.format("%.1f",progress/(float)totalSize2*100)+"%");
        }

        progress++;
        }//while문
        bufferedOutput.write(bytes);  //소켓 버퍼에 write
        }//if문
        bufferedOutput.flush();
        }catch(IOException e){
        e.printStackTrace();
        }finally{
        try{
        if(bufferedOutput!=null)
        bufferedOutput.close();
        if(dataInput!=null)
        dataInput.close();
        if(dataOutput!=null)
        dataOutput.close();
        if(socket!=null)
        socket.close();
        }catch(IOException e){
        e.printStackTrace();
        }
        System.out.println("file transfer complete");
        }
        }
        }
        }

}//ServerSend 클래스