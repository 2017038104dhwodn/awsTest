import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.imagebuilder.model.Ami;
import com.amazonaws.services.imagebuilder.model.GetImageRequest;
import com.amazonaws.services.imagebuilder.model.GetImageResult;
import com.amazonaws.services.sagemaker.model.DescribeImageRequest;
import com.amazonaws.services.sagemaker.model.DescribeImageResult;

import java.util.Scanner;

public class awsTest {
    /*
     * Cloud Computing, Data Computing Laboratory
     * Department of Computer Science
     * Chungbuk National University
     */
    static AmazonEC2 ec2;
    //Scanner scanner = new Scanner(System.in);

    private static void init() throws Exception {
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        ec2 = AmazonEC2ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-east-2") /* check the region at AWS console */
                .build();
    }

    public static void listInstances() {
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

    public static void main(String[] args) throws Exception {
        init();
        Scanner menu = new Scanner(System.in);
        //Scanner id_string = new Scanner(System.in);
        //int number = 0;
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
            System.out.println("                                 99. quit                   ");
            System.out.println("------------------------------------------------------------");
            System.out.print("Enter an integer: ");

            switch (menu.nextInt()) {
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
                case 99:
                    System.exit(0);
                    break;
            }
            //break;
        }
    }

    public static void listImages() {
        /*System.out.println("Listing AMI Images...");
        DescribeImagesRequest describeImagesRequest = new DescribeImagesRequest();
        DescribeImagesResult describeImagesResult = ec2.describeImages(describeImagesRequest);
        for(Image ami: describeImagesResult.getImages()) {
            System.out.printf("[name] %s", ami.getName());
        }*/
        DescribeImagesRequest request = new DescribeImagesRequest().withOwners("self");
        DescribeImagesResult imagesResult = ec2.describeImages(request);
        for (Image image: imagesResult.getImages()) {
            System.out.printf("[name] %15s [id] %15s [Owner] %15s\n", image.getName(), image.getImageId(), image.getOwnerId());
        }
    }

    public static void availableRegions() {
        DescribeRegionsResult regionsResult = ec2.describeRegions();

        for (Region region : regionsResult.getRegions()) {
            System.out.printf(
                    "[region] %15s,  [endpoint] %s \n", region.getRegionName(), region.getEndpoint()
            );
        }
    }

    public static void availableZones() {
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

    public static void CreateInstances() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter ami id: ");
        String amiId = scanner.next();
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
                .withImageId(amiId)
                .withInstanceType(InstanceType.T2Micro)
                .withMaxCount(1)
                .withMinCount(1);
        RunInstancesResult run_response = ec2.runInstances((runInstancesRequest));
        String reservation_id = run_response.getReservation().getInstances().get(0).getInstanceId();

        System.out.println("Successfully started EC2 instance "+reservation_id+"based on AMI "+amiId);
    }

    public static void rebootInstances() {
        Scanner scanner = new Scanner((System.in));
        System.out.print("Enter instances id: ");
        String InstanceId = scanner.next();
        System.out.println("Rebooting... "+InstanceId);
        RebootInstancesRequest request = new RebootInstancesRequest().withInstanceIds(InstanceId);
        RebootInstancesResult response = ec2.rebootInstances(request);
        System.out.println("Successfully reboot instnace..."+InstanceId);
    }

    public static void stopInstances() {
        Scanner scanner = new Scanner((System.in));
        System.out.print("Enter instances id: ");
        String InstanceId = scanner.next();
        System.out.println("Stopping... "+InstanceId);
        StopInstancesRequest request = new StopInstancesRequest().withInstanceIds(InstanceId);
        ec2.stopInstances(request);
        System.out.println("Successfully stop instnace..."+InstanceId);
    }

    public static void startInstances() {
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