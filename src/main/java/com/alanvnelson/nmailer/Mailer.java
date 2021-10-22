package com.alanvnelson.nmailer;

import com.moandjiezana.toml.Toml;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.commons.io.FileUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

/**
 *
 * @author anelson
 */
public class Mailer
{

    private String username;
    private String password;
    private String hostname;
    private int portNumber;
    private boolean setSSL;

    private String subject;
    private String emailBody = "";
    private InternetAddress fromAddress = null;
    private String fromName;
    private InternetAddress recipient = null;
    private final List<InternetAddress> bccList = new ArrayList<>();
    private final List<File> attachments = new ArrayList<>();
    private boolean output = false;
    private InternetAddress replyAddress = null;
    private InternetAddress bounceAddress = null;
    private boolean sendIndividually = false;

    public Mailer()
    {
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    public void setEmailBody(File bodyFile)
    {

        if (bodyFile != null)
        {
            try
            {
                this.emailBody = FileUtils.readFileToString(bodyFile, Charset.defaultCharset());
            } catch (IOException ex)
            {
                Logger.getLogger(Mailer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void setFromAddress(String fromAddress)
    {
        try
        {
            this.fromAddress = new InternetAddress(fromAddress);
        } catch (AddressException ex)
        {
            Logger.getLogger(Mailer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setFromAddress(InternetAddress fromAddress)
    {
        this.fromAddress = fromAddress;
    }

    public void setFromName(String fromName)
    {
        this.fromName = fromName;
    }

    public void setRecipient(InternetAddress recipient)
    {
        this.recipient = recipient;
    }

    public void setRecipient(String recipient)
    {
        try
        {
            this.recipient = new InternetAddress(recipient);
        } catch (AddressException ex)
        {
            Logger.getLogger(Mailer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setBccList(File bccFile)
    {
        try
        {
            List<String> emailAddresses = FileUtils.readLines(bccFile, Charset.defaultCharset());
            for (String address : emailAddresses)
            {
                bccList.add(new InternetAddress(address));
            }
        } catch (IOException | AddressException ex)
        {
            Logger.getLogger(Mailer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setAttachments(String[] attachments)
    {
        if (attachments != null)
        {

            for (String file : attachments)
            {
                File f = new File(file);
                if (f.exists())
                {
                    this.attachments.add(f);
                } else
                {
                    try
                    {
                        throw new java.io.FileNotFoundException(f.getAbsolutePath() + " does not exist!");
                    } catch (FileNotFoundException ex)
                    {
                        ex.printStackTrace();
                        System.exit(127);
                    }
                }
            }
        }
    }

    public void setOutput(boolean output)
    {
        this.output = output;
    }

    public void setSendIndividually(boolean sendIndividually)
    {
        this.sendIndividually = sendIndividually;
    }

    public void setReplyAddress(InternetAddress replyAddress)
    {
        this.replyAddress = replyAddress;
    }

    public void setBounceAddress(InternetAddress bounceAddress)
    {
        this.bounceAddress = bounceAddress;
    }

    public void sendMail()
    {
        try
        {

            if (!sendIndividually)
            {
                System.out.println("yes bcc");
                MultiPartEmail email = this.generateMultiPartEmail();
                if (recipient != null)
                {
                    email.addTo(recipient.getAddress());
                }
                if (bccList.size() > 0)
                {
                    email.setBcc(bccList);
                }

                email.send();
            } else
            {
                int i = 1;
                if (recipient != null)
                {
                    bccList.add(0, recipient);
                }
                for (InternetAddress emailAddress : bccList)
                {
                    System.out.println("mf indi");
                    MultiPartEmail email = this.generateMultiPartEmail();
                    if (output)
                    {
                        System.out.println("Sending out email to " + emailAddress.getAddress() + " (" + i + " out of " + bccList.size() + "   " + String.format("%.2f", (i / (float) bccList.size() * 100)) + "% complete )");
                    }

                    email.addTo(emailAddress.getAddress());
                    email.send();
                    i++;
                }
            }

        } catch (EmailException ex)
        {
            ex.printStackTrace();
        }

    }

    private MultiPartEmail generateMultiPartEmail()
    {
        try
        {
            MultiPartEmail email = new MultiPartEmail();
            email.setHostName(this.hostname);
            email.setSmtpPort(this.portNumber);
            email.setAuthenticator(new DefaultAuthenticator(this.username, this.password));
            email.setSSLOnConnect(this.setSSL);
            email.setFrom(fromAddress.getAddress(), fromName);
            email.setSubject(subject);
            email.setMsg(emailBody);

            if (replyAddress != null)
            {
                email.addReplyTo(replyAddress.getAddress());
            }
            if (bounceAddress != null)
            {
                email.setBounceAddress(bounceAddress.getAddress());
            }
            for (File attachment : attachments)
            {
                email.attach(attachment);
            }
            return email;
        } catch (EmailException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    public void readConfig()
    {
        Toml toml = new Toml().read(new File("./config.toml"));

        this.username = toml.getString("server.username");
        this.password = toml.getString("server.password");
        this.hostname = toml.getString("server.hostname");
        this.portNumber = Math.toIntExact(toml.getLong("server.smtpport"));
        this.setSSL = toml.getBoolean("server.setSSLonConnect");
    }
}
