// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

contract MusicDonationSplitter {

    mapping(address => uint256) public artistBalances;

    event DonationReceived(address indexed donor, uint256 amount);
    event FundsDistributed(address indexed artist, uint256 amount);

    // Donate and specify split percentages
    function donate(address[] calldata artists, uint256[] calldata percentages) external payable {
        require(msg.value > 0, "No ETH sent");
        require(artists.length == percentages.length, "Mismatched arrays");

        uint256 totalPercent = 0;

        for (uint256 i = 0; i < artists.length; i++) {
            require(percentages[i] > 0, "Invalid split");
            uint256 share = (msg.value * percentages[i]) / 1000;
            artistBalances[artists[i]] += share;
            totalPercent += percentages[i];
        }

        require(totalPercent == 1000, "Splits must sum to 100.0%");
        emit DonationReceived(msg.sender, msg.value);
    }

    function getBalance(address artist) public view returns (uint256) {
        return artistBalances[artist];
    }

    function withdrawBalance() public {
        uint256 balance = artistBalances[msg.sender];
        require(balance > 0, "Nothing to withdraw");
        (bool sent, bytes memory data) = msg.sender.call{value: balance}("");
        require(sent, "Transfer failed");
        artistBalances[msg.sender] = 0;

        emit FundsDistributed(msg.sender, balance);
    }

    // Donate only through donate
    receive() external payable {
        revert("Send ETH via donate() only");
    }
}