package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.time.LocalDateTime;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static final int PAGES = 5;

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) {
        HabrCareerParse careerParse = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> list = careerParse.list(PAGE_LINK);
        list.forEach(System.out::println);
    }

    @Override
    public List<Post> list(String link) {
        List<Post> list = new ArrayList<>();
        try {
            for (int i = 1; i <= PAGES; i++) {
                Connection connection = Jsoup.connect(String.format("%s%s", link, "?page=" + i));
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    Element titleElement = row.select(".vacancy-card__title").first();
                    Element linkElement = titleElement.child(0);
                    String vacancyName = titleElement.text();
                    String fullLink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                    System.out.printf("%s %s%n", vacancyName, fullLink);
                    Element dateElement = row.select(".vacancy-card__date").first();
                    Element dateTimeElement = Objects.requireNonNull(dateElement).child(0);
                    String dataTime = dateTimeElement.attr("datetime");
                    System.out.println("Дата вакансии: " + dataTime);
                    LocalDateTime localDateTime = dateTimeParser.parse(dateTimeElement.attr("datetime"));
                    System.out.println("Дата вакансии в формате для LocalDataTime: " + localDateTime);
                    String vacancyDescription = retrieveDescription(fullLink);
                    System.out.println("Детальное описание вакансии: \n" + vacancyDescription);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

        private static String retrieveDescription(String link) {
        Connection connection = Jsoup.connect(link);
        String text = null;
        try {
            Document document = connection.get();
            Element descriptionElement = document.select(".style-ugc").first();
            text = Objects.requireNonNull(descriptionElement).text();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }
}
