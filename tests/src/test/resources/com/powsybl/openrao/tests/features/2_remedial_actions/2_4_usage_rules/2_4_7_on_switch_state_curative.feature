# Copyright (c) 2026, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

Feature: 2.4.7: Activate curative remedial actions depending on the state of a switch
  # This feature covers the OnSwitchState usage rule on curative remedial actions. The switch state
  # must be read from the network as it is at the curative instant, i.e. with the preventive and
  # automaton remedial actions applied.
  # All scenarios use the network of test case 2.1.3, where the coupler "DDE3AA11 DDE3AA12 1" is
  # initially open, and a single contingency "Defaut FR1 FR2".
  # The curative remedial action cra_open_nl1_nl2 opens line "NNL1AA1  NNL2AA1  1". It is guarded by
  # an AndUsageRule combining OnContingencyState(curative, "Defaut FR1 FR2") and
  # OnSwitchState(curative, coupler). The curative CNEC "NNL1AA1  NNL2AA1  1 - Defaut FR1 FR2 -
  # curative" (threshold +/-100 A) is violated after the contingency, whatever the state of the
  # coupler (333 A when it is open, 127 A when it is closed): only cra_open_nl1_nl2 can secure it.
  # The security status of the RAO therefore tells whether the curative remedial action was available.
  # Scenarios go by pairs: the first one of each pair expects the coupler in the state it actually is
  # at the curative instant (SECURED), the second one expects the opposite state (UNSECURED).

  @fast @rao @ac @contingency-scenarios @secure-flow
  Scenario: 2.4.7.1: Curative action available, switch state given by the initial network
  # No preventive nor automaton remedial action acts on the coupler: it stays open, as in the initial
  # network. cra_open_nl1_nl2 expects it to be open, so it is available and secures the network.
    Given network file is "epic15/TestCase12NodesWithSwitch.uct"
    Given crac file is "extra_features/Crac_SWITCH_4.json"
    Given configuration file is "common/RaoParameters_posMargin_ampere.json"
    When I launch rao
    Then the execution details should be "The RAO only went through first preventive"
    Then its security status should be "SECURED"
    Then 0 remedial actions are used in preventive
    Then 1 remedial actions are used after "Defaut FR1 FR2" at "curative"
    Then the remedial action "cra_open_nl1_nl2" is used after "Defaut FR1 FR2" at "curative"
    Then the margin on cnec "NNL1AA1  NNL2AA1  1 - Defaut FR1 FR2 - curative" after CRA should be 100.0 A
    Then the worst margin is 100.0 A

  @fast @rao @ac @contingency-scenarios @secure-flow
  Scenario: 2.4.7.2: Curative action unavailable, switch state given by the initial network
  # Same as 2.4.7.1, but cra_open_nl1_nl2 expects the coupler to be closed: it is not available.
    Given network file is "epic15/TestCase12NodesWithSwitch.uct"
    Given crac file is "extra_features/Crac_SWITCH_5.json"
    Given configuration file is "common/RaoParameters_posMargin_ampere.json"
    When I launch rao
    Then its security status should be "UNSECURED"
    Then 0 remedial actions are used in preventive
    Then 0 remedial actions are used after "Defaut FR1 FR2" at "curative"
    Then the margin on cnec "NNL1AA1  NNL2AA1  1 - Defaut FR1 FR2 - curative" after CRA should be -233.1 A
    Then the worst margin is -233.1 A

  @fast @rao @ac @contingency-scenarios @secure-flow
  Scenario: 2.4.7.3: Curative action available, switch closed by a preventive remedial action
  # The preventive CNEC "FFR2AA1  DDE3AA11 1 - preventive" is violated in the initial network: the
  # preventive remedial action pra_close_de3 closes the coupler to secure it. No automaton acts on the
  # coupler. cra_open_nl1_nl2 expects it to be closed, so it is available and secures the network.
    Given network file is "epic15/TestCase12NodesWithSwitch.uct"
    Given crac file is "extra_features/Crac_SWITCH_6.json"
    Given configuration file is "common/RaoParameters_posMargin_ampere.json"
    When I launch rao
    Then the execution details should be "The RAO only went through first preventive"
    Then its security status should be "SECURED"
    Then 1 remedial actions are used in preventive
    Then the remedial action "pra_close_de3" is used in preventive
    Then 1 remedial actions are used after "Defaut FR1 FR2" at "curative"
    Then the remedial action "cra_open_nl1_nl2" is used after "Defaut FR1 FR2" at "curative"
    Then the margin on cnec "NNL1AA1  NNL2AA1  1 - Defaut FR1 FR2 - curative" after CRA should be 100.0 A
    Then the worst margin is 100.0 A

  @fast @rao @ac @contingency-scenarios @secure-flow
  Scenario: 2.4.7.4: Curative action unavailable, switch closed by a preventive remedial action
  # Same as 2.4.7.3, but cra_open_nl1_nl2 expects the coupler to be open, as it is in the initial
  # network. It must not be available: this scenario would fail if the switch state were read from
  # the initial network instead of the network after preventive remedial actions.
    Given network file is "epic15/TestCase12NodesWithSwitch.uct"
    Given crac file is "extra_features/Crac_SWITCH_7.json"
    Given configuration file is "common/RaoParameters_posMargin_ampere.json"
    When I launch rao
    Then its security status should be "UNSECURED"
    Then 1 remedial actions are used in preventive
    Then the remedial action "pra_close_de3" is used in preventive
    Then 0 remedial actions are used after "Defaut FR1 FR2" at "curative"
    Then the margin on cnec "NNL1AA1  NNL2AA1  1 - Defaut FR1 FR2 - curative" after CRA should be -27.1 A
    Then the worst margin is -27.1 A

  @fast @rao @ac @contingency-scenarios @secure-flow
  Scenario: 2.4.7.5: Curative action available, switch closed by an automaton
  # No preventive remedial action acts on the coupler, but the automaton ara_close_de3 closes it after
  # the contingency. cra_open_nl1_nl2 expects it to be closed, so it is available and secures the
  # network.
    Given network file is "epic15/TestCase12NodesWithSwitch.uct"
    Given crac file is "extra_features/Crac_SWITCH_8.json"
    Given configuration file is "common/RaoParameters_posMargin_ampere.json"
    When I launch rao
    Then the execution details should be "The RAO only went through first preventive"
    Then its security status should be "SECURED"
    Then 0 remedial actions are used in preventive
    Then 1 remedial actions are used after "Defaut FR1 FR2" at "auto"
    Then the remedial action "ara_close_de3" is used after "Defaut FR1 FR2" at "auto"
    Then 1 remedial actions are used after "Defaut FR1 FR2" at "curative"
    Then the remedial action "cra_open_nl1_nl2" is used after "Defaut FR1 FR2" at "curative"
    Then the margin on cnec "NNL1AA1  NNL2AA1  1 - Defaut FR1 FR2 - curative" after CRA should be 100.0 A
    Then the worst margin is 100.0 A

  @fast @rao @ac @contingency-scenarios @secure-flow
  Scenario: 2.4.7.6: Curative action unavailable, switch closed by an automaton
  # Same as 2.4.7.5, but cra_open_nl1_nl2 expects the coupler to be open, as it is in the initial
  # network and after preventive remedial actions. It must not be available: this scenario would fail
  # if the switch state were read from the network before automatons.
    Given network file is "epic15/TestCase12NodesWithSwitch.uct"
    Given crac file is "extra_features/Crac_SWITCH_9.json"
    Given configuration file is "common/RaoParameters_posMargin_ampere.json"
    When I launch rao
    Then its security status should be "UNSECURED"
    Then 0 remedial actions are used in preventive
    Then 1 remedial actions are used after "Defaut FR1 FR2" at "auto"
    Then the remedial action "ara_close_de3" is used after "Defaut FR1 FR2" at "auto"
    Then 0 remedial actions are used after "Defaut FR1 FR2" at "curative"
    Then the margin on cnec "NNL1AA1  NNL2AA1  1 - Defaut FR1 FR2 - curative" after CRA should be -27.1 A
    Then the worst margin is -27.1 A
