# Copyright (c) 2026, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

Feature: 2.4.8: AND/OR usage rules on automatons
  # This feature covers AndUsageRule and OrUsageRule at the AUTO instant, for range action automatons
  # and for topological automatons.
  # Besides its availability, the usage rules of a range action automaton define the flow cnecs it
  # shifts its set-point for: all the flow cnecs of the automaton state for an OnContingencyState,
  # OnInstant or OnSwitchState usage rule, only the constraining flow cnecs for an OnConstraint or
  # OnFlowConstraintInCountry usage rule. AND/OR usage rules are explored recursively:
  # - an OR considers all the flow cnecs of the state if one of its usage rules does, the union of
  #   the constraining flow cnecs of its usage rules otherwise;
  # - an AND considers all the flow cnecs of the state if all its usage rules do, the union of the
  #   constraining flow cnecs of its usage rules otherwise.
  #
  # Scenarios 2.4.8.1 to 2.4.8.6 are variants of test case 1.2.2.2: the automaton pst_be, after
  # contingency co2_be1_be3, secures both auto cnecs "BBE2AA1  FFR3AA1  1 - co2_be1_be3 - auto"
  # (initial margin -107.6 A) and "FFR4AA1  DDE1AA1  1 - co2_be1_be3 - auto" (initial margin -41.2 A)
  # when it considers both of them (tap -8). After contingency co1_fr2_fr3_1, the auto cnec
  # "FFR4AA1  DDE1AA1  1 - co1_fr2_fr3_1 - auto" has an initial margin of -22.0 A, which pst_be
  # secures (tap -6) when it is available after this contingency.
  # The auto cnec "BBE2AA1  FFR3AA1  1 - co2_be1_be3 - auto - never violated" has huge thresholds.

  @fast @rao @ac @contingency-scenarios @max-min-margin
  Scenario: 2.4.8.1: PST automaton with an OR of two OnContingencyState usage rules
  # The OR considers all the flow cnecs of the state: pst_be behaves as in test case 1.2.2.2 after
  # co2_be1_be3, and is also used after co1_fr2_fr3_1.
    Given network file is "common/TestCase16Nodes.uct"
    Given crac file is "extra_features/Crac_AUTO_PST_1.json"
    Given configuration file is "epic13/RaoParameters_maxMargin_ampere_absolute_threshold.json"
    When I launch rao
    Then the execution details should be "The RAO only went through first preventive"
    Then its security status should be "SECURED"
    Then 0 remedial actions are used in preventive
    Then 1 remedial actions are used after "co2_be1_be3" at "auto"
    Then the remedial action "pst_be" is used after "co2_be1_be3" at "auto"
    Then the tap of PstRangeAction "pst_be" should be -8 after "co2_be1_be3" at "auto"
    Then the margin on cnec "BBE2AA1  FFR3AA1  1 - co2_be1_be3 - auto" after ARA should be 98.9 A
    Then the margin on cnec "FFR4AA1  DDE1AA1  1 - co2_be1_be3 - auto" after ARA should be 0.2 A
    Then 1 remedial actions are used after "co1_fr2_fr3_1" at "auto"
    Then the remedial action "pst_be" is used after "co1_fr2_fr3_1" at "auto"
    Then the tap of PstRangeAction "pst_be" should be -6 after "co1_fr2_fr3_1" at "auto"
    Then the margin on cnec "FFR4AA1  DDE1AA1  1 - co1_fr2_fr3_1 - auto" after ARA should be 0.4 A
    Then the worst margin is 0.2 A

  @fast @rao @ac @contingency-scenarios @max-min-margin
  Scenario: 2.4.8.2: PST automaton with an AND of an OnContingencyState and a met OnConstraint usage rule
  # The auto cnec "FFR4AA1  DDE1AA1  1 - co2_be1_be3 - auto" is violated, so pst_be is triggered. It only
  # considers this cnec, which is the most limiting one when pst_be is shifted: the result is the same
  # as in test case 1.2.2.2.
    Given network file is "common/TestCase16Nodes.uct"
    Given crac file is "extra_features/Crac_AUTO_PST_2.json"
    Given configuration file is "epic13/RaoParameters_maxMargin_ampere_absolute_threshold.json"
    When I launch rao
    Then the execution details should be "The RAO only went through first preventive"
    Then its security status should be "UNSECURED"
    Then 0 remedial actions are used in preventive
    Then 1 remedial actions are used after "co2_be1_be3" at "auto"
    Then the remedial action "pst_be" is used after "co2_be1_be3" at "auto"
    Then the tap of PstRangeAction "pst_be" should be -8 after "co2_be1_be3" at "auto"
    Then the margin on cnec "BBE2AA1  FFR3AA1  1 - co2_be1_be3 - auto" after ARA should be 98.9 A
    Then the margin on cnec "FFR4AA1  DDE1AA1  1 - co2_be1_be3 - auto" after ARA should be 0.2 A
    Then 0 remedial actions are used after "co1_fr2_fr3_1" at "auto"
    Then the worst margin is -22.0 A

  @fast @rao @ac @contingency-scenarios @max-min-margin
  Scenario: 2.4.8.3: PST automaton with an AND of an OnContingencyState and an unmet OnConstraint usage rule
  # The auto cnec "BBE2AA1  FFR3AA1  1 - co2_be1_be3 - auto - never violated" is secure, so pst_be is
  # not triggered.
    Given network file is "common/TestCase16Nodes.uct"
    Given crac file is "extra_features/Crac_AUTO_PST_3.json"
    Given configuration file is "epic13/RaoParameters_maxMargin_ampere_absolute_threshold.json"
    When I launch rao
    Then the execution details should be "The RAO only went through first preventive"
    Then its security status should be "UNSECURED"
    Then 0 remedial actions are used in preventive
    Then 0 remedial actions are used after "co2_be1_be3" at "auto"
    Then the margin on cnec "BBE2AA1  FFR3AA1  1 - co2_be1_be3 - auto" after ARA should be -107.6 A
    Then the margin on cnec "FFR4AA1  DDE1AA1  1 - co2_be1_be3 - auto" after ARA should be -41.2 A
    Then 0 remedial actions are used after "co1_fr2_fr3_1" at "auto"
    Then the worst margin is -107.6 A

  @fast @rao @ac @contingency-scenarios @max-min-margin
  Scenario: 2.4.8.4: PST automaton with an OR of a met and an unmet OnConstraint usage rule
  # The auto cnec "BBE2AA1  FFR3AA1  1 - co2_be1_be3 - auto" is violated, so pst_be is triggered. It only
  # considers this cnec and the never violated one: it stops shifting as soon as "BBE2AA1  FFR3AA1  1"
  # is secure (tap -5), although "FFR4AA1  DDE1AA1  1 - co2_be1_be3 - auto" is still violated.
    Given network file is "common/TestCase16Nodes.uct"
    Given crac file is "extra_features/Crac_AUTO_PST_4.json"
    Given configuration file is "epic13/RaoParameters_maxMargin_ampere_absolute_threshold.json"
    When I launch rao
    Then the execution details should be "The RAO only went through first preventive"
    Then its security status should be "UNSECURED"
    Then 0 remedial actions are used in preventive
    Then 1 remedial actions are used after "co2_be1_be3" at "auto"
    Then the remedial action "pst_be" is used after "co2_be1_be3" at "auto"
    Then the tap of PstRangeAction "pst_be" should be -5 after "co2_be1_be3" at "auto"
    Then the margin on cnec "BBE2AA1  FFR3AA1  1 - co2_be1_be3 - auto" after ARA should be 21.5 A
    Then the margin on cnec "FFR4AA1  DDE1AA1  1 - co2_be1_be3 - auto" after ARA should be -15.3 A
    Then 0 remedial actions are used after "co1_fr2_fr3_1" at "auto"
    Then the worst margin is -22.0 A

  @fast @rao @ac @contingency-scenarios @max-min-margin
  Scenario: 2.4.8.5: PST automaton with an OR of usage rules unmet after co2_be1_be3
  # The OR combines an OnContingencyState after co1_fr2_fr3_1 and an OnConstraint on the never violated
  # auto cnec: pst_be is not triggered after co2_be1_be3, but it is after co1_fr2_fr3_1.
    Given network file is "common/TestCase16Nodes.uct"
    Given crac file is "extra_features/Crac_AUTO_PST_5.json"
    Given configuration file is "epic13/RaoParameters_maxMargin_ampere_absolute_threshold.json"
    When I launch rao
    Then the execution details should be "The RAO only went through first preventive"
    Then its security status should be "UNSECURED"
    Then 0 remedial actions are used in preventive
    Then 0 remedial actions are used after "co2_be1_be3" at "auto"
    Then the margin on cnec "BBE2AA1  FFR3AA1  1 - co2_be1_be3 - auto" after ARA should be -107.6 A
    Then the margin on cnec "FFR4AA1  DDE1AA1  1 - co2_be1_be3 - auto" after ARA should be -41.2 A
    Then 1 remedial actions are used after "co1_fr2_fr3_1" at "auto"
    Then the remedial action "pst_be" is used after "co1_fr2_fr3_1" at "auto"
    Then the tap of PstRangeAction "pst_be" should be -6 after "co1_fr2_fr3_1" at "auto"
    Then the margin on cnec "FFR4AA1  DDE1AA1  1 - co1_fr2_fr3_1 - auto" after ARA should be 0.4 A
    Then the worst margin is -107.6 A

  @fast @rao @ac @contingency-scenarios @max-min-margin
  Scenario: 2.4.8.6: PST automaton with an AND usage rule containing a nested OR usage rule
  # AND(OnContingencyState, OR(OnConstraint on the never violated cnec, OnConstraint on the violated
  # cnec "FFR4AA1  DDE1AA1  1 - co2_be1_be3 - auto")): pst_be is triggered, and behaves as in 2.4.8.2.
    Given network file is "common/TestCase16Nodes.uct"
    Given crac file is "extra_features/Crac_AUTO_PST_6.json"
    Given configuration file is "epic13/RaoParameters_maxMargin_ampere_absolute_threshold.json"
    When I launch rao
    Then the execution details should be "The RAO only went through first preventive"
    Then its security status should be "UNSECURED"
    Then 0 remedial actions are used in preventive
    Then 1 remedial actions are used after "co2_be1_be3" at "auto"
    Then the remedial action "pst_be" is used after "co2_be1_be3" at "auto"
    Then the tap of PstRangeAction "pst_be" should be -8 after "co2_be1_be3" at "auto"
    Then the margin on cnec "BBE2AA1  FFR3AA1  1 - co2_be1_be3 - auto" after ARA should be 98.9 A
    Then the margin on cnec "FFR4AA1  DDE1AA1  1 - co2_be1_be3 - auto" after ARA should be 0.2 A
    Then 0 remedial actions are used after "co1_fr2_fr3_1" at "auto"
    Then the worst margin is -22.0 A

  # Scenarios 2.4.8.7 to 2.4.8.10 are variants of feature 2.4.6: the coupler "DDE3AA11 DDE3AA12 1" is
  # initially open and closed in preventive by pra_close_de3. After contingency "Defaut FR1 FR2", the
  # auto cnec "NNL1AA1  NNL2AA1  1 - Defaut FR1 FR2 - auto" is violated (-27.1 A).

  @fast @rao @ac @contingency-scenarios @secure-flow
  Scenario: 2.4.8.7: Topological automaton with an OR of an unmet OnSwitchState and a met OnConstraint usage rule
  # ara_open_nl1_nl2 is guarded by OR(AND(OnContingencyState, OnSwitchState expecting the coupler open),
  # OnConstraint on the violated auto cnec). The coupler is closed by pra_close_de3, but the auto cnec
  # is violated, so the automaton is triggered and secures the network as in 2.4.6.1.
    Given network file is "epic15/TestCase12NodesWithSwitch.uct"
    Given crac file is "extra_features/Crac_AUTO_SWITCH_1.json"
    Given configuration file is "common/RaoParameters_posMargin_ampere.json"
    When I launch rao
    Then the execution details should be "The RAO only went through first preventive"
    Then its security status should be "SECURED"
    Then 1 remedial actions are used in preventive
    Then the remedial action "pra_close_de3" is used in preventive
    Then 1 remedial actions are used after "Defaut FR1 FR2" at "auto"
    Then the remedial action "ara_open_nl1_nl2" is used after "Defaut FR1 FR2" at "auto"
    Then the margin on cnec "NNL1AA1  NNL2AA1  1 - Defaut FR1 FR2 - auto" after ARA should be 100.0 A
    Then the margin on cnec "BBE2AA1  FFR3AA1  1 - Defaut FR1 FR2 - auto" after ARA should be 417.5 A
    Then the worst margin is 100.0 A

  @fast @rao @ac @contingency-scenarios @secure-flow
  Scenario: 2.4.8.8: Topological automaton with an OR of unmet OnSwitchState and OnConstraint usage rules
  # Same as 2.4.8.7, but the OnConstraint is defined on the secure auto cnec
  # "BBE2AA1  FFR3AA1  1 - Defaut FR1 FR2 - auto": the automaton is not triggered, as in 2.4.6.2.
    Given network file is "epic15/TestCase12NodesWithSwitch.uct"
    Given crac file is "extra_features/Crac_AUTO_SWITCH_2.json"
    Given configuration file is "common/RaoParameters_posMargin_ampere.json"
    When I launch rao
    Then its security status should be "UNSECURED"
    Then 1 remedial actions are used in preventive
    Then the remedial action "pra_close_de3" is used in preventive
    Then 0 remedial actions are used after "Defaut FR1 FR2" at "auto"
    Then the margin on cnec "NNL1AA1  NNL2AA1  1 - Defaut FR1 FR2 - auto" after ARA should be -27.1 A
    Then the margin on cnec "BBE2AA1  FFR3AA1  1 - Defaut FR1 FR2 - auto" after ARA should be 437.6 A
    Then the worst margin is -27.1 A

  @fast @rao @ac @contingency-scenarios @secure-flow
  Scenario: 2.4.8.9: PST automaton with an AND of an OnContingencyState and a met OnSwitchState usage rule
  # ara_pst_be is guarded by AND(OnContingencyState, OnSwitchState expecting the coupler closed). The
  # coupler is closed by pra_close_de3, so the automaton is triggered. The AND considers all the flow
  # cnecs of the state: the automaton shifts until the auto cnec on "NNL1AA1  NNL2AA1  1" is secure.
    Given network file is "epic15/TestCase12NodesWithSwitch.uct"
    Given crac file is "extra_features/Crac_AUTO_SWITCH_3.json"
    Given configuration file is "common/RaoParameters_posMargin_ampere.json"
    When I launch rao
    Then the execution details should be "The RAO only went through first preventive"
    Then its security status should be "SECURED"
    Then 1 remedial actions are used in preventive
    Then the remedial action "pra_close_de3" is used in preventive
    Then 1 remedial actions are used after "Defaut FR1 FR2" at "auto"
    Then the remedial action "ara_pst_be" is used after "Defaut FR1 FR2" at "auto"
    Then the tap of PstRangeAction "ara_pst_be" should be 5 after "Defaut FR1 FR2" at "auto"
    Then the margin on cnec "NNL1AA1  NNL2AA1  1 - Defaut FR1 FR2 - auto" after ARA should be 2.0 A
    Then the margin on cnec "BBE2AA1  FFR3AA1  1 - Defaut FR1 FR2 - auto" after ARA should be 350.4 A
    Then the worst margin is 2.0 A

  @fast @rao @ac @contingency-scenarios @secure-flow
  Scenario: 2.4.8.10: PST automaton with an AND of an OnContingencyState and an unmet OnSwitchState usage rule
  # Same as 2.4.8.9, but the OnSwitchState expects the coupler open: the automaton is not triggered.
    Given network file is "epic15/TestCase12NodesWithSwitch.uct"
    Given crac file is "extra_features/Crac_AUTO_SWITCH_4.json"
    Given configuration file is "common/RaoParameters_posMargin_ampere.json"
    When I launch rao
    Then its security status should be "UNSECURED"
    Then 1 remedial actions are used in preventive
    Then the remedial action "pra_close_de3" is used in preventive
    Then 0 remedial actions are used after "Defaut FR1 FR2" at "auto"
    Then the margin on cnec "NNL1AA1  NNL2AA1  1 - Defaut FR1 FR2 - auto" after ARA should be -27.1 A
    Then the margin on cnec "BBE2AA1  FFR3AA1  1 - Defaut FR1 FR2 - auto" after ARA should be 437.6 A
    Then the worst margin is -27.1 A
