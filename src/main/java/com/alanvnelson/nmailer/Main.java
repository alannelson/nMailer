/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alanvnelson.nmailer;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.commons.cli.*;

/**
 *
 * @author anelson
 */
public class Main
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        Options options = new Options();
        Option help = new Option("help", "Prints this message");
        Option output = new Option("output", "display output");
        Option subject = Option.builder("subject")
                .argName("subject")
                .hasArg()
                .desc("email subject")
                .optionalArg(false)
                .build();
        Option body = Option.builder("body")
                .argName("body")
                .hasArg()
                .desc("either plain text or html")
                .optionalArg(false)
                .build();
        Option fromAddress = Option.builder("fromAddress")
                .argName("fromAddress")
                .hasArg()
                .desc("address of sender")
                .optionalArg(false)
                .build();
        Option fromName = Option.builder("fromName")
                .argName("fromName")
                .hasArg()
                .desc("name of sender")
                .optionalArg(false)
                .build();
        Option replyAddress = Option.builder("replyAddress")
                .argName("replyAddresd")
                .hasArg()
                .desc("reply address")
                .optionalArg(false)
                .build();
        Option bounceAddress = Option.builder("bounceAddress")
                .argName("bounceAddress")
                .hasArg()
                .desc("address to send bounced messages")
                .optionalArg(false)
                .build();
        Option toAddress = Option.builder("recipient")
                .argName("to")
                .hasArg()
                .desc("address of recipient")
                .optionalArg(false)
                .build();
        Option toList = Option.builder("recipientList")
                .argName("toList")
                .hasArg()
                .desc("file containing recipients separated by newline. if -sendIndividually not set or set to false, this list will be BCC'd")
                .optionalArg(false)
                .build();
        Option sendIndividually = Option.builder("sendIndividually")
                .argName("sendIndividually")
                .hasArg()
                .desc("If true, will send mail to each recipient as an individual email. Defaults to false.")
                .optionalArg(false)
                .build();
        Option attachments = Option.builder("attachment")
                .argName("attachement")
                .hasArgs()
                .desc("path of file(s) to attach to the email")
                .optionalArg(false)
                .build();

        options.addOption(help);
        options.addOption(output);
        options.addOption(subject);
        options.addOption(body);
        options.addOption(fromAddress);
        options.addOption(fromName);
        options.addOption(toAddress);
        options.addOption(toList);
        options.addOption(attachments);
        options.addOption(replyAddress);
        options.addOption(bounceAddress);
        options.addOption(output);
        options.addOption(sendIndividually);

        CommandLineParser parser = new DefaultParser();
        try
        {
            CommandLine cli = parser.parse(options, args);
            if (cli.hasOption("help"))
            {
                // initialise the member variable
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("nMailer", options);
                System.exit(0);
            }
            if (!cli.hasOption("fromAddress") || (!cli.hasOption("recipient") && !cli.hasOption("recipientList")))
            {
                System.out.println("Missing required parameter, see help");
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("nMailer", options);
                System.exit(127);
            }

            Mailer mailer = new Mailer();

            mailer.readConfig();
            mailer.setFromAddress(new InternetAddress(cli.getOptionValue("fromAddress")));
           
            if (cli.hasOption("fromName"))
            {
                mailer.setSubject(cli.getOptionValue("fromName"));
            }
            if (cli.hasOption("recipient"))
            {
               mailer.setRecipient(new InternetAddress(cli.getOptionValue("recipient")));
            }
            if (cli.hasOption("recipientList"))
            {
               mailer.setBccList(new File(cli.getOptionValue("recipientList")));
            }
            if(cli.hasOption("attachment"))
            {
                mailer.setAttachments(cli.getOptionValues("attachment"));
            }
            if (cli.hasOption("output"))
            {
                mailer.setOutput(Boolean.parseBoolean(cli.getOptionValue("output")));
            }
            if (cli.hasOption("sendIndividually"))
            {
                mailer.setOutput(Boolean.parseBoolean(cli.getOptionValue("sendIndividually")));
            }
            if (cli.hasOption("subject"))
            {
                mailer.setSubject(cli.getOptionValue("subject"));
            }
            if(cli.hasOption("body"))
            {
                mailer.setEmailBody(new File(cli.getOptionValue("body")));
            }
            
            if (cli.hasOption("bounceAddress"))
            {
                mailer.setBounceAddress(new InternetAddress(cli.getOptionValue("bounceAddress")));
            }
            if (cli.hasOption("replyAddress"))
            {
                mailer.setReplyAddress(new InternetAddress(cli.getOptionValue("replyAddress")));
            }

            mailer.sendMail();
        } catch (ParseException exp)
        {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        } catch (AddressException ex)
        {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
