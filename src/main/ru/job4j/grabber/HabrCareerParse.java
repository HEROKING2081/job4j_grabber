package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;
import ru.job4j.grabber.utils.DateTimeParser;

import java.io.IOException;
import java.util.Objects;
import java.time.LocalDateTime;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static final DateTimeParser DATE_TIME_PARSER = new HabrCareerDateTimeParser();

    private static final int PAGES = 5;

    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= PAGES; i++) {
            Connection connection = Jsoup.connect(String.format("%s%s", PAGE_LINK, "?page=" + i));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("%s %s%n", vacancyName, link);
                Element dateElement = row.select(".vacancy-card__date").first();
                Element dateTimeElement = Objects.requireNonNull(dateElement).child(0);
                String dataTime = dateTimeElement.attr("datetime");
                System.out.println("Дата вакансии: " + dataTime);
                LocalDateTime localDateTime = DATE_TIME_PARSER.parse(dataTime);
                System.out.println("Дата вакансии в формате для LocalDataTime: " + localDateTime);
            });
        }
    }
}
