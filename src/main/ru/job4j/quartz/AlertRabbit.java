package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

    public static void main(String[] args) throws ClassNotFoundException {
        Properties properties = new Properties();
        try (BufferedReader readProperties = new BufferedReader(new FileReader("rabbit.properties"))) {
            properties.load(readProperties);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        Class.forName(properties.getProperty("rabbit.db-driver-class-name"));
        try (Connection connection = DriverManager.getConnection(
                properties.getProperty("rabbit.db-url"),
                properties.getProperty("rabbit.db-username"),
                properties.getProperty("rabbit.db-password")
        )) {
            List<Long> store = new ArrayList<>();
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("connection", connection);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(properties.getProperty("rabbit.interval")))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context)  {
            System.out.println("Rabbit runs here ...");
            Connection cn = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            try (PreparedStatement statement =
                         cn.prepareStatement("insert into rabbit (created_date) values (?)")) {
                Date date = new Date(System.currentTimeMillis());
                statement.setDate(1, date);
                statement.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
