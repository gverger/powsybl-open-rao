# Copyright (c) 2026, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

Feature: 2.4.5: Activate remedial actions when at least one of several usage rules combined with OR is met
  # This feature covers the OrUsageRule gate logic combination: a remedial action guarded by an
  # OrUsageRule is available for a given state if AT LEAST ONE of the UsageRules it combines is
  # itself defined for that state.

  @fast @rao @ac @contingency-scenarios @max-min-margin
  Scenario: 2.4.5.1: Preventive OR of two OnConstraint usage rules, one condition met
  # pst_be's OrUsageRule combines the same two OnConstraint usage rules as in test case 2.4.4.2:
  # one on a violated preventive CNEC, and one on a CNEC that can never be violated (a threshold of
  # +/-100000 A). Where the AndUsageRule of 2.4.4.2 made pst_be unavailable, the OrUsageRule only
  # requires one of its UsageRules to hold, so pst_be is available and the results are the same as
  # in 2.4.1.7 and 2.4.4.1.
    Given network file is "common/TestCase16Nodes.uct"
    Given crac file is "extra_features/Crac_OR_1.json"
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
  Scenario: 2.4.5.2: Preventive OR of two OnConstraint usage rules, no condition met
  # pst_be's OrUsageRule combines two OnConstraint usage rules on CNECs that can never be violated
  # (thresholds of +/-100000 A). Since none of its UsageRules holds, pst_be never becomes available,
  # and the results are the same as in 2.4.4.2.
    Given network file is "common/TestCase16Nodes.uct"
    Given crac file is "extra_features/Crac_OR_2.json"
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
  Scenario: 2.4.5.3: OR of two different kinds of usage rules (OnInstant and OnConstraint)
  # An OrUsageRule can combine leaf usage rules of different kinds. Here, OnInstant(preventive) OR
  # OnConstraint(preventive, cnec never violated): the OnConstraint never holds, but OnInstant(preventive)
  # is enough to make pst_be available in preventive (and in no other state). Results are the same as
  # in 2.4.1.7 and 2.4.5.1.
    Given network file is "common/TestCase16Nodes.uct"
    Given crac file is "extra_features/Crac_OR_3.json"
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
  Scenario: 2.4.5.4: OR with a nested AND usage rule
  # pst_be's OrUsageRule combines an OnConstraint on a CNEC that can never be violated (a threshold
  # of +/-100000 A) with a nested AndUsageRule, itself combining OnInstant(preventive) and an
  # OnConstraint on the violated preventive CNEC. The OnConstraint never holds, but the nested
  # AndUsageRule does, so the OrUsageRule holds and pst_be is available in preventive. The nested
  # AndUsageRule is decisive here: without it, pst_be would be unavailable (as in 2.4.5.2). Results
  # are the same as in 2.4.1.7 and 2.4.5.1.
    Given network file is "common/TestCase16Nodes.uct"
    Given crac file is "extra_features/Crac_OR_4.json"
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
