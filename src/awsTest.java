import com.amazonaws.AmazonClientException; //Aws API
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.jcraft.jsch.*;   //SSH client api
import java.io.*;   //file return

import java.util.Scanner;

import static java.lang.Thread.sleep;

public class awsTest {
    /*
     * Cloud Computing, Data Computing Laboratory
     * Department of Computer Science
     * Chungbuk National University
     */
    static AmazonEC2 ec2;   //AWS EC2 Module

    private static void init() throws Exception {   //verify my personal information
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();  //get my aws key
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        ec2 = AmazonEC2ClientBuilder.standard() //set my region
                .withCredentials(credentialsProvider)
                .withRegion("us-east-2") /* check the region at AWS console */
                .build();
    }

    public static void listInstances() {    //List my ec2 Instance
        System.out.println("Listing instances....");
        boolean done = false;
        DescribeInstancesRequest request = new DescribeInstancesRequest();
        while (!done) {
            DescribeInstancesResult response = ec2.describeInstances(request);
            for (Reservation reservation : response.getReservations()) {
                for (Instance instance : reservation.getInstances()) {
                    System.out.printf(
                                    "[id] %s, " +
                                    "[AMI] %s, " +
                                    "[type] %s, " +
                                    "[state] %10s, " +
                                    "[monitoring state] %s",
                            instance.getInstanceId(),
                            instance.getImageId(),
                            instance.getInstanceType(),
                            instance.getState().getName(),
                            instance.getMonitoring().getState());
                }
                System.out.println();
            }
            request.setNextToken(request.getNextToken());

            if(response.getNextToken() == null)
                done=true;
        }
    }

    public static void main(String[] args) throws Exception {   //main function
        init(); //you can use aws to manage instances and images dynamically
        Scanner menu = new Scanner(System.in);
        while (true) {
            System.out.println("                                                            ");
            System.out.println("                                                            ");
            System.out.println("------------------------------------------------------------");
            System.out.println("           Amazon AWS Control Panel using SDK               ");
            System.out.println("                                                            ");
            System.out.println("  Cloud Computing, Computer Science Department              ");
            System.out.println("                           at Chungbuk National University  ");
            System.out.println("------------------------------------------------------------");
            System.out.println("  1. list instance                2. available zones         ");
            System.out.println("  3. start instance               4. available regions      ");
            System.out.println("  5. stop instance                6. create instance        ");
            System.out.println("  7. reboot instance              8. list images            ");
            System.out.println("  9. condor_status                10. run instance and command");
            System.out.println("  11. create Image                12. terminate instance");
            System.out.println("  99. quit                   ");
            System.out.println("------------------------------------------------------------");
            System.out.print("Enter an integer: ");

            switch (menu.nextInt()) {   //If you choose number, the number's function will execute.
                case 1:
                    listInstances();
                    break;
                case 2:
                    availableZones();
                    break;
                case 3:
                    startInstances();
                    break;
                case 4:
                    availableRegions();
                    break;
                case 5:
                    stopInstances();
                    break;
                case 6:
                    CreateInstances();
                    break;
                case 7:
                    rebootInstances();
                    break;
                case 8:
                    listImages();
                    break;
                case 9:
                    condor_status();
                    break;
                case 10:
                    RunCommand();
                    break;
                case 11:
                    createImage();
                    break;
                case 12:
                    terminateInstance();
                    break;
                case 99:
                    System.exit(0);
                    break;
            }
            //break;
        }
    }

    public static void terminateInstance() {    //Terminate your instance
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Instance id: ");
        String InstanceId = scanner.next();

        TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
        ec2.terminateInstances(terminateInstancesRequest.withInstanceIds(InstanceId));

        System.out.println("Successfully terminated EC2 instance "+InstanceId);
    }

    public static void createImage() {  //Create an image based on your existing instances
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Instance id: ");
        String InstanceId = scanner.next();
        CreateImageRequest createImageRequest = new CreateImageRequest();
        createImageRequest.setInstanceId(InstanceId);
        System.out.print("Enter Ami name: ");
        String AmiId = scanner.next();
        createImageRequest.setName(AmiId);
        CreateImageResult createImageResult = ec2.createImage(createImageRequest);
        String createdImageId = createImageResult.getImageId();
        System.out.println("Successfully started EC2 AMIs "+createdImageId+"based on Instance "+InstanceId);
    }

    public static void RunCommand() throws JSchException, IOException, InterruptedException {   //you can use command line through this console
        Scanner scanner = new Scanner(System.in);   //but, you have one chances.
        JSch jsch=new JSch();   //If you want more results, you should execute "Runcommand" or Enter 10
        jsch.addIdentity("/home/dhwodnojw/.ssh/HTCondorSecurity.pem");  //my ssh key. only can use my pc.
        JSch.setConfig("StrictHostKeyChecking", "no");

//enter your own EC2 instance IP here
        Session session=jsch.getSession("ec2-user", "18.116.21.120", 22);
        session.connect();

//run stuff
        System.out.print("Write Command: ");
        //String command;
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(scanner.next());
        channel.setErrStream(System.err);
        channel.connect();

        InputStream input = channel.getInputStream();
//start reading the input from the executed commands on the shell
        byte[] tmp = new byte[1024];
        while (true) {
            while (input.available() > 0) { //command result
                int i = input.read(tmp, 0, 1024);
                if (i < 0) break;
                System.out.print(new String(tmp, 0, i));
            }
            if (channel.isClosed()){
                System.out.println("exit-status: " + channel.getExitStatus());
                break;
            }
            sleep(1000);
        }

        channel.disconnect();   //disconnect ssh
        session.disconnect();
    }

    public static void condor_status() throws JSchException, IOException, InterruptedException {    //same description of RunCommand()
        JSch jsch=new JSch();
        jsch.addIdentity("/home/dhwodnojw/.ssh/HTCondorSecurity.pem");
        JSch.setConfig("StrictHostKeyChecking", "no");

//enter your own EC2 instance IP here
        Session session=jsch.getSession("ec2-user", "18.116.21.120", 22);
        session.connect();

//run stuff
        String command = "condor_status";
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.setErrStream(System.err);
        channel.connect();

        InputStream input = channel.getInputStream();
//start reading the input from the executed commands on the shell
        byte[] tmp = new byte[1024];
        while (true) {
            while (input.available() > 0) {
                int i = input.read(tmp, 0, 1024);
                if (i < 0) break;
                System.out.print(new String(tmp, 0, i));
            }
            if (channel.isClosed()){
                System.out.println("exit-status: " + channel.getExitStatus());
                break;
            }
            sleep(1000);
        }

        channel.disconnect();
        session.disconnect();
    }

    public static void listImages() {   //List my images
        DescribeImagesRequest request = new DescribeImagesRequest().withOwners("self");
        DescribeImagesResult imagesResult = ec2.describeImages(request);
        for (Image image: imagesResult.getImages()) {
            System.out.printf("[name] %15s [id] %15s [Owner] %15s\n", image.getName(), image.getImageId(), image.getOwnerId());
        }
    }

    public static void availableRegions() { //list your available regions that can use your resources
        DescribeRegionsResult regionsResult = ec2.describeRegions();

        for (Region region : regionsResult.getRegions()) {
            System.out.printf(
                    "[region] %15s,  [endpoint] %s \n", region.getRegionName(), region.getEndpoint()
            );
        }
    }

    public static void availableZones() {   //list your available zones that can use your resources
        DescribeAvailabilityZonesResult zonesResult = ec2.describeAvailabilityZones();

        for(AvailabilityZone zone: zonesResult.getAvailabilityZones()) {
            System.out.printf(
                    "[id] %s "+
                       "[region] %s "+
                       "[zone] %s " +
                       "[state] %s",
                    zone.getZoneId(),
                    zone.getRegionName(),
                    zone.getZoneName(),
                    zone.getState()
            );
            System.out.println();
        }
    }

    public static void CreateInstances() {  //Create your instance based on existing your AMIs.
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter ami id: ");
        String amiId = scanner.next();
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
                .withImageId(amiId)
                .withInstanceType(InstanceType.T2Micro)
                .withMaxCount(1)
                .withMinCount(1)
                ;
        RunInstancesResult run_response = ec2.runInstances((runInstancesRequest));
        String reservation_id = run_response.getReservation().getInstances().get(0).getInstanceId();

        System.out.println("Successfully started EC2 instance "+reservation_id+" based on AMI "+amiId);
    }

    public static void rebootInstances() {  //Reboot your instances
        Scanner scanner = new Scanner((System.in));
        System.out.print("Enter instances id: ");
        String InstanceId = scanner.next();
        System.out.println("Rebooting... "+InstanceId);
        RebootInstancesRequest request = new RebootInstancesRequest().withInstanceIds(InstanceId);
        RebootInstancesResult response = ec2.rebootInstances(request);
        System.out.println("Successfully reboot instnace..."+InstanceId);
    }

    public static void stopInstances() {    //Stopping your instances
        Scanner scanner = new Scanner((System.in));
        System.out.print("Enter instances id: ");
        String InstanceId = scanner.next();
        System.out.println("Stopping... "+InstanceId);
        StopInstancesRequest request = new StopInstancesRequest().withInstanceIds(InstanceId);
        ec2.stopInstances(request);
        System.out.println("Successfully stop instnace..."+InstanceId);
    }

    public static void startInstances() {   //Start your instances
        Scanner scanner = new Scanner((System.in));
        System.out.print("Enter instances id: ");
        String InstanceId = scanner.next();
        System.out.println("Strating..." + InstanceId);
        StartInstancesRequest request = new StartInstancesRequest().withInstanceIds(InstanceId);
        ec2.startInstances((request));
        System.out.println("Successfully started instnace"+InstanceId);
        //scanner.close();
    }
}