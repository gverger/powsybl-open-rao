# Copyright (c) 2026, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

Feature: 2.4.4: Activate remedial actions only when several usage rules are combined with AND
  # This feature covers the AndUsageRule gate logic combination: a remedial action guarded by an
  # AndUsageRule is only available for a given state if ALL of the UsageRules it combines are
  # themselves defined for that state.

  @fast @rao @ac @contingency-scenarios @max-min-margin
  Scenario: 2.4.4.1: Preventive AND of two OnConstraint usage rules, both conditions met
  # This is a copy of test case 2.4.1.7, where pst_be's single OnConstraint usage rule is replaced
  # by an AndUsageRule combining two OnConstraint usage rules on the same (violated) preventive
  # CNEC. Since both conditions are met, pst_be should behave exactly as in 2.4.1.7.
    Given network file is "common/TestCase16Nodes.uct"
    Given crac file is "extra_features/Crac_AND_1.json"
    Given configuration file is "common/RaoParameters_maxMargin_ampere.json"
    When I launch rao
    Then the execution details should be "The RAO only went through first preventive"
    Then its security status should be "SECURED"
    Then 1 remedial actions are used in preventive
    Then the remedial action "pst_be" is used in preventive
    Then the tap of PstRangeAction "pst_be" should be 16 in preventive
    Then 0 remedial actions are used after "co1_fr2_fr3_1" at "curative"
    Then the worst margin is 82 A
    Then the margin on cnec "BBE1AA1  FFR5AA1  1 - preventive" after PRA should be 82 A
    Then the margin on cnec "BBE2AA1  FFR3AA1  1 - co1_fr2_fr3_1 - curative" after CRA should be 245 A
    Then the margin on cnec "FFR3AA1  FFR5AA1  1 - co1_fr2_fr3_1 - curative" after CRA should be 345 A

  @fast @rao @ac @contingency-scenarios @max-min-margin
  Scenario: 2.4.4.2: Preventive AND of two OnConstraint usage rules, one condition not met
  # pst_be's AndUsageRule combines the same violated preventive CNEC as 2.4.4.1 with a second
  # OnConstraint on a CNEC that can never be violated (a threshold of +/-100000 A). Because the
  # AndUsageRule requires ALL of its UsageRules to hold, pst_be never becomes available, even
  # though one of the two conditions is genuinely met. This is the same network, contingency and
  # CNECs as 2.4.4.1, so with pst_be unavailable the results fall back to the network's natural,
  # unconstrained-by-a-remedial-action state (as in test case 2.4.3.2, where pst_be is also never
  # available for this same network and contingency).
    Given network file is "common/TestCase16Nodes.uct"
    Given crac file is "extra_features/Crac_AND_2.json"
    Given configuration file is "common/RaoParameters_maxMargin_ampere.json"
    When I launch rao
    Then the execution details should be "The RAO only went through first preventive"
    Then its security status should be "UNSECURED"
    Then 0 remedial actions are used in preventive
    Then 0 remedial actions are used after "co1_fr2_fr3_1" at "curative"
    Then the worst margin is -49 A
    Then the margin on cnec "BBE1AA1  FFR5AA1  1 - preventive" after PRA should be -49 A
    Then the margin on cnec "BBE2AA1  FFR3AA1  1 - co1_fr2_fr3_1 - curative" after CRA should be 693 A
    Then the margin on cnec "FFR3AA1  FFR5AA1  1 - co1_fr2_fr3_1 - curative" after CRA should be 79 A

  @fast @rao @ac @contingency-scenarios @max-min-margin
  Scenario: 2.4.4.3: AND of two different kinds of usage rules (OnInstant and OnConstraint)
  # An AndUsageRule can combine leaf usage rules of different kinds, as long as they can all be
  # defined for the same state. Here, OnInstant(preventive) AND OnConstraint(preventive, cnec) is
  # equivalent to a plain OnConstraint(preventive, cnec) usage rule, since OnInstant(preventive) is
  # defined exactly for the preventive state and for no other. Results are the same as in 2.4.1.7
  # and 2.4.4.1.
    Given network file is "common/TestCase16Nodes.uct"
    Given crac file is "extra_features/Crac_AND_3.json"
    Given configuration file is "common/RaoParameters_maxMargin_ampere.json"
    When I launch rao
    Then the execution details should be "The RAO only went through first preventive"
    Then its security status should be "SECURED"
    Then 1 remedial actions are used in preventive
    Then the remedial action "pst_be" is used in preventive
    Then the tap of PstRangeAction "pst_be" should be 16 in preventive
    Then 0 remedial actions are used after "co1_fr2_fr3_1" at "curative"
    Then the worst margin is 82 A
    Then the margin on cnec "BBE1AA1  FFR5AA1  1 - preventive" after PRA should be 82 A
    Then the margin on cnec "BBE2AA1  FFR3AA1  1 - co1_fr2_fr3_1 - curative" after CRA should be 245 A
    Then the margin on cnec "FFR3AA1  FFR5AA1  1 - co1_fr2_fr3_1 - curative" after CRA should be 345 A

  @fast @rao @ac @contingency-scenarios @max-min-margin
  Scenario: 2.4.4.4: AND with a nested OR usage rule
  # pst_be's AndUsageRule combines OnInstant(preventive) with a nested OrUsageRule, itself combining
  # an OnConstraint on the violated preventive CNEC and an OnConstraint on a CNEC that can never be
  # violated (a threshold of +/-100000 A). The nested OrUsageRule holds thanks to its first
  # OnConstraint, and OnInstant(preventive) holds as well, so the AndUsageRule holds and pst_be is
  # available in preventive. The nested OrUsageRule is decisive here: evaluated as an AND, it would
  # make pst_be unavailable (as in 2.4.4.2). Results are the same as in 2.4.1.7 and 2.4.4.1.
    Given network file is "common/TestCase16Nodes.uct"
    Given crac file is "extra_features/Crac_AND_4.json"
    Given configuration file is "common/RaoParameters_maxMargin_ampere.json"
    When I launch rao
    Then the execution details should be "The RAO only went through first preventive"
    Then its security status should be "SECURED"
    Then 1 remedial actions are used in preventive
    Then the remedial action "pst_be" is used in preventive
    Then the tap of PstRangeAction "pst_be" should be 16 in preventive
    Then 0 remedial actions are used after "co1_fr2_fr3_1" at "curative"
    Then the worst margin is 82 A
    Then the margin on cnec "BBE1AA1  FFR5AA1  1 - preventive" after PRA should be 82 A
    Then the margin on cnec "BBE2AA1  FFR3AA1  1 - co1_fr2_fr3_1 - curative" after CRA should be 245 A
    Then the margin on cnec "FFR3AA1  FFR5AA1  1 - co1_fr2_fr3_1 - curative" after CRA should be 345 A
