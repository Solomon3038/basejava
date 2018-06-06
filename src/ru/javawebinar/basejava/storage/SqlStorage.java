package ru.javawebinar.basejava.storage;

import ru.javawebinar.basejava.exception.NotExistStorageException;
import ru.javawebinar.basejava.model.*;
import ru.javawebinar.basejava.sql.SqlHelper;

import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.sql.DriverManager.*;

public class SqlStorage implements Storage {

    public final SqlHelper sqlHelper;

    public SqlStorage(String dbUrl, String dbUser, String dbPassword) {
        sqlHelper = new SqlHelper(() -> getConnection(dbUrl, dbUser, dbPassword));
    }

    @Override
    public void save(Resume resume) {
        sqlHelper.transactionalExecute(conn -> {
                    try (PreparedStatement ps = conn.prepareStatement("INSERT INTO resume (uuid, full_name) VALUES (?,?)")) {
                        ps.setString(1, resume.getUuid());
                        ps.setString(2, resume.getFullName());
                        ps.execute();
                    }
                    doInsert(resume, conn);
                    return null;
                }
        );
    }

    @Override
    public Resume get(String uuid) {
        return sqlHelper.execute("" +
                "SELECT * FROM resume r " +
                "LEFT JOIN contact c  ON r.uuid = c.resume_uuid " +
                "WHERE r.uuid = ?", ps -> {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new NotExistStorageException(uuid);
            }
            Resume resume = new Resume(uuid, rs.getString("full_name"));
            do {
                String value = rs.getString("value");
                String category = rs.getString("type");
                if (value != null) {
                    switchCategoryType(value, category, resume);
                }
            } while (rs.next());
            return resume;
        });
    }

    @Override
    public void update(Resume resume) {
        sqlHelper.transactionalExecute(conn -> {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE resume SET full_name = ? WHERE uuid = ?")) {
                ps.setString(1, resume.getFullName());
                ps.setString(2, resume.getUuid());
                int update = ps.executeUpdate();
                if (update == 0) {
                    throw new NotExistStorageException(resume.getUuid());
                }
            }
            doDelete(resume.getUuid(), conn.prepareStatement("DELETE FROM contact WHERE resume_uuid = ?"));
            doInsert(resume, conn);
            return null;
        });
    }

    @Override
    public void delete(String uuid) {
        sqlHelper.execute("DELETE FROM resume WHERE uuid = ?", ps -> {
            doDelete(uuid, ps);
            return null;
        });
    }

    @Override
    public int size() {
        return sqlHelper.execute("SELECT COUNT (*) FROM resume", ps -> {
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        });
    }

    @Override
    public void clear() {
        sqlHelper.execute("DELETE FROM resume", ps -> {
            ps.execute();
            return null;
        });
    }

    @Override
    public List<Resume> getAllSorted() {
        Map<String, Resume> map = new HashMap<>();
        return sqlHelper.transactionalExecute(conn -> {
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM resume")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String uuid = rs.getString("uuid");
                    map.put(uuid, new Resume(uuid, rs.getString("full_name")));
                }
            }
            try (PreparedStatement psContact = conn.prepareStatement("SELECT * FROM contact")) {
                ResultSet rsContact = psContact.executeQuery();
                while (rsContact.next()) {
                    String uuid = rsContact.getString("resume_uuid");
                    String value = rsContact.getString("value");
                    String category = rsContact.getString("type");
                    switchCategoryType(value, category, map.get(uuid));
                }
            }
            return map.values().stream().sorted().collect(Collectors.toList());
        });
    }

    private void switchCategoryType(String value, String category, Resume resume) {
        switch (category) {
            case "OBJECTIVE":
            case "PERSONAL":
                resume.addCategory(SectionType.valueOf(category),
                        new StringCategory(value));
                break;
            case "ACHIEVEMENT":
            case "QUALIFICATIONS":
                resume.addCategory(SectionType.valueOf(category),
                        new ListCategory(Arrays.asList(value.split("/n"))));
                break;
            case "ADDRESS":
            case "PHONE":
            case "SKYPE":
            case "MAIL":
            case "LINKEDIN":
            case "GITHUB":
            case "STACKOVERFLOW":
                resume.addContact(ContactsType.valueOf(category), value);
                break;
            default:
                break;
        }
    }

    private void doInsert(Resume resume, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO contact (resume_uuid, type, value) VALUES (?,?,?)")) {
            for (Map.Entry<ContactsType, String> e : resume.getContacts().entrySet()) {
                ps.setString(1, resume.getUuid());
                ps.setString(2, e.getKey().name());
                ps.setString(3, e.getValue());
                ps.addBatch();
            }
            for (Map.Entry<SectionType, Category> e : resume.getSections().entrySet()) {
                ps.setString(1, resume.getUuid());
                ps.setString(2, e.getKey().name());
                if (e.getValue() instanceof StringCategory) {
                    ps.setString(3, ((StringCategory) e.getValue()).getContent());
                } else if (e.getValue() instanceof ListCategory) {
                    ps.setString(3, ((ListCategory) e.getValue()).getItems().stream().collect(Collectors.joining("/n")));
                }
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void doDelete(String uuid, PreparedStatement ps) throws SQLException {
        ps.setString(1, uuid);
        int delete = ps.executeUpdate();
        if (delete == 0) {
            throw new NotExistStorageException(uuid);
        }
    }
}
