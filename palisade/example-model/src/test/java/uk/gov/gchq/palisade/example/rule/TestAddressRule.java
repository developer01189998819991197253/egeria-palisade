package uk.gov.gchq.palisade.example.rule;

import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.example.common.Purpose;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.rule.Rule;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeThat;

@RunWith(Theories.class)
public class TestAddressRule extends TestCommonRuleTheories {

    @DataPoint
    public static final AddressRule rule = new AddressRule();

    @Theory
    public void testUnchangedWithProfileAccess(Rule<Employee> rule, final Employee record, final User user, final Context context) {
        // Given - Purpose == PROFILE_ACCESS
        assumeThat(context.getPurpose(), is(Purpose.PROFILE_ACCESS.name()));
        // Given - Employee.Uid == User.Uid
        assumeThat(record.getUid(), is(user.getUserId()));

        // When
        Employee recordWithRule = rule.apply(new Employee(record), user, context);

        // Then
        assertThat(recordWithRule, equalTo(record));
    }

    @Theory
    public void testUnchangedWithHealthPurpose(Rule<Employee> rule, final Employee record, final User user, final Context context) {
        // Given - Purpose == HEALTH_SCREENING
        assumeThat(context.getPurpose(), is(Purpose.HEALTH_SCREENING.name()));

        // When
        Employee recordWithRule = rule.apply(new Employee(record), user, context);

        Employee maskedRecord = new Employee(record);
        maskedRecord.setAddress(null);
        maskedRecord.setWorkLocation(recordWithRule.getWorkLocation());
        // Then
        assertThat(recordWithRule.getAddress(), not(equalTo(record.getAddress())));
        assertThat(recordWithRule, is(maskedRecord));
    }

    @Theory
    public void testAddressRedacted(Rule<Employee> rule, final Employee record, final User user, final Context context) {
        // Given - Doesn't satisfy PROFILE_ACCESS rule
        assumeFalse(context.getPurpose().equals(Purpose.PROFILE_ACCESS.name()) && record.getUid().equals(user.getUserId()));
        // Given - Purpose != ""
        assumeThat(context.getPurpose(), not(isEmptyString()));

        // When
        Employee recordWithRule = rule.apply(new Employee(record), user, context);

        // Then - Expected
        Employee redactedRecord = new Employee(record);
        redactedRecord.setAddress(null);
        // Then - Observed
        assertThat(recordWithRule, is(redactedRecord));
    }
}
