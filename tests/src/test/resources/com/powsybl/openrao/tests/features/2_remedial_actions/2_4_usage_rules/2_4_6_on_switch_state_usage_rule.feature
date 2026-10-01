# Copyright (c) 2026, RTE (http://www.rte-france.com)
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

Feature: 2.4.6: Activate remedial actions depending on the state of a switch
  # This feature covers the OnSwitchState usage rule: a remedial action guarded by an OnSwitchState
  # usage rule is only available if the switch is in the expected (open or closed) state. The switch
  # state must be read from the network as it is when the usage rule is evaluated, i.e. with the
  # remedial actions of the previous instants applied, and not from the initial network.
  # All scenarios use the network of test case 2.1.3, where the coupler "DDE3AA11 DDE3AA12 1" is
  # initially open, and where closing it in preventive (pra_close_de3) secures the network.
  # The automaton ara_open_nl1_nl2 opens line "NNL1AA1  NNL2AA1  1". It is guarded by an
  # AndUsageRule combining OnContingencyState(auto, "Defaut FR1 FR2") and OnSwitchState(auto, coupler).
  # In 2.4.6.1 and 2.4.6.2, the auto CNEC "NNL1AA1  NNL2AA1  1 - Defaut FR1 FR2 - auto" (threshold
  # +/-100 A) is violated after the contingency (127 A flowing through the line, even with
  # pra_close_de3 applied): only ara_open_nl1_nl2 can secure it, by opening the line. The security
  # status of the RAO therefore tells whether the automaton has been triggered or not.

  @fast @rao @ac @contingency-scenarios @secure-flow
  Scenario: 2.4.6.1: Automaton triggered by a switch closed by a preventive remedial action
  # ara_open_nl1_nl2 expects the coupler to be closed. It is open in the initial network, but
  # pra_close_de3 closes it in preventive, so the automaton is triggered and secures the auto CNEC.
    Given network file is "epic15/TestCase12NodesWithSwitch.uct"
    Given crac file is "extra_features/Crac_SWITCH_1.json"
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
  Scenario: 2.4.6.2: Automaton not triggered because a preventive remedial action closed the switch
  # Same as 2.4.6.1, but ara_open_nl1_nl2 expects the coupler to be open. It is open in the initial
  # network, but closed by pra_close_de3 in preventive, so the automaton must not be triggered: this
  # scenario would fail if the switch state were read from the initial network. The auto CNEC on
  # "NNL1AA1  NNL2AA1  1" stays violated, and the RAO result is unsecure although pra_close_de3 is
  # applied as in 2.4.6.1.
    Given network file is "epic15/TestCase12NodesWithSwitch.uct"
    Given crac file is "extra_features/Crac_SWITCH_2.json"
    Given configuration file is "common/RaoParameters_posMargin_ampere.json"
    When I launch rao
    Then its security status should be "UNSECURED"
    Then 1 remedial actions are used in preventive
    Then the remedial action "pra_close_de3" is used in preventive
    Then 0 remedial actions are used after "Defaut FR1 FR2" at "auto"
    Then the margin on cnec "NNL1AA1  NNL2AA1  1 - Defaut FR1 FR2 - auto" after ARA should be -27.1 A
    Then the margin on cnec "BBE2AA1  FFR3AA1  1 - Defaut FR1 FR2 - auto" after ARA should be 437.6 A
    Then the worst margin is -27.1 A

  @fast @rao @ac @contingency-scenarios @max-min-margin
  Scenario: 2.4.6.3: Automaton triggered by a switch left open
  # Control case for 2.4.6.2: ara_open_nl1_nl2 expects the coupler to be open, and there is no
  # preventive remedial action, so the coupler stays open and the automaton is triggered. The network
  # cannot be secured without pra_close_de3: a max-min-margin objective function is used so that the
  # post-contingency perimeters are optimized anyway (with a secure-flow objective function, the RAO
  # stops when the preventive perimeter cannot be secured).
    Given network file is "epic15/TestCase12NodesWithSwitch.uct"
    Given crac file is "extra_features/Crac_SWITCH_3.json"
    Given configuration file is "common/RaoParameters_maxMargin_ampere.json"
    When I launch rao
    Then its security status should be "UNSECURED"
    Then 0 remedial actions are used in preventive
    Then 1 remedial actions are used after "Defaut FR1 FR2" at "auto"
    Then the remedial action "ara_open_nl1_nl2" is used after "Defaut FR1 FR2" at "auto"
    Then the margin on cnec "BBE2AA1  FFR3AA1  1 - Defaut FR1 FR2 - auto" after ARA should be 1010.2 A
    Then the worst margin is -667.1 A
