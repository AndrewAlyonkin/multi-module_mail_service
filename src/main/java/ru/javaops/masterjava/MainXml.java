package ru.javaops.masterjava;

import com.google.common.io.Resources;
import one.util.streamex.StreamEx;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.schema.Payload;
import ru.javaops.masterjava.xml.schema.Project;
import ru.javaops.masterjava.xml.schema.User;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.Schemas;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Alenkin Andrew
 * oxqq@ya.ru
 */
public class MainXml {
    public static void main(String[] args) throws Exception {
        if (args.length !=2) {
            System.exit(1);
        }
        URL url = Resources.getResource(args[1]);
        MainXml mainXml = new MainXml();
        String project = args[0];

        Set<User> users = mainXml.parse(project, url);
//        String result = parseToHtml(users, project, Paths.get("out/users,html"));
//        System.out.println(result);
    }

    private Set<User> parse(String project, URL url) throws Exception {
        JaxbParser parser = new JaxbParser(ObjectFactory.class);
        parser.setSchema(Schemas.ofClasspath("payload.xsd"));
        try(InputStream is = url.openStream()) {
            Payload payload = parser.unmarshal(is);
            Project resultProject = StreamEx.of(payload.getProjects().getProject())
                    .filter(p -> p.getName().equals(project))
                    .findAny()
                    .orElseThrow(IllegalAccessException::new);
            Set<Project.Group> groups = new HashSet<>(resultProject.getGroup());
            return StreamEx.of(payload.getUsers().getUser())
                    .filter(user -> StreamEx.of(user.getGroupRefs())
                    .findAny(groups::contains)
                            .isPresent())
                    .collect(Collectors.toCollection(TreeSet::new));
        }
    }
}
