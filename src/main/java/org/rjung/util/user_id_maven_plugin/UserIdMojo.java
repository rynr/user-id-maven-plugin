package org.rjung.util.user_id_maven_plugin;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Set a property (default is {@code user.id}) to the user-id that is running
 * the maven-call.
 */
@Mojo(name = "user-id", defaultPhase = LifecyclePhase.INITIALIZE)
public class UserIdMojo extends AbstractMojo {

    private static final String PROPERTY_USER_NAME = "user.name";

    @Parameter(property = "passwdPath", defaultValue = "/etc/passwd")
    private String passwdPath;

    @Parameter(property = "columnSeparator", defaultValue = ":")
    private String columnSeparator;

    @Parameter(property = "usernameColumn", defaultValue = "0")
    private int usernameColumn;

    @Parameter(property = "uidColumn", defaultValue = "2")
    private int uidColumn;

    @Parameter(property = "defaultUid", defaultValue = "0")
    private String defaultUid;

    @Parameter(property = "propertyName", defaultValue = "user.id")
    private String propertyName;

    /**
     * Main execution-method providing the user-id from a given file with
     * {@code passwd}-structure.
     *
     * @throws MojoExecutionException
     *             Thrown if there are issues opening {@code passwdPath}
     */
    public void execute() throws MojoExecutionException {
        try {
            String userName = System.getProperty(PROPERTY_USER_NAME);
            String uid = getUid(userName);
            getLog().debug("setting property `user.id` to " + uid);
            System.setProperty(propertyName, uid);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * Open the {@code passwdPath} and search for the entry which has the
     * parameter {@code userName} in the column {@code usernameColumn}. Of this
     * line, the column {@code uidColumn} is returned. If no line is found the
     * {@code defaultUid} is returned.
     *
     * @param userName
     *            The username to search it's user-id for
     * @return The user-id of the found {@code userName} or the
     *         {@code defaultUid}
     * @throws IOException
     *             Thrown if there are issues opening {@code passwdPath}
     */
    private String getUid(String userName) throws IOException {
        try (InputStream is = new FileInputStream(passwdPath);
                InputStreamReader isr = new InputStreamReader(is,
                        Charset.forName("ASCII"));
                BufferedReader reader = new BufferedReader(isr);) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(columnSeparator);
                if (columns.length >= Math.max(usernameColumn, uidColumn)
                        && userName.equals(columns[usernameColumn])) {
                    return columns[uidColumn];
                }
            }
        }
        return defaultUid;
    }

}
