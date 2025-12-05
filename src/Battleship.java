import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class Battleship extends JFrame {

    // ------ CELL VARIABLES ------ \\
    private static final int SIZE = 10;
    private static final int CELL_SIZE = 40;


    // ------ FLEET STORAGE CONTROL ------ \\
    private Ship[] playerOneFleet;
    private Ship[] playerTwoFleet;

    private boolean[] playerOnePlacedShips;
    private boolean[] playerTwoPlacedShips;


    // ------ SHIP PLACEMENT VARIABLES ------ \\
    private boolean inShipPlacementPhase = true;
    private boolean playerOnePlacing = true;
    private int currentShipIndex = 0;
    private boolean placeShipHorizontal = true;


    // ------- BOARD, PANEL AND BUTTONS ------- \\
    private JPanel playerOnePanel;
    private JPanel playerTwoPanel;

    private Board playerOneBoard;
    private Board playerTwoBoard;

    private JButton carrierButton;
    private JButton battleshipButton;
    private JButton cruiserButton;
    private JButton destroyerButton;
    private JButton randomShipPlacementButton;


    // ------- TURN LOGIC VARIABLES -------- \\
    private boolean playerOnesTurn = true;
    private boolean turnLocked = false;
    private JLabel turnLabel;
    private Timer countdownTimer;
    private int countdown;
    private static int TURN_DELAY_SECONDS = 1;
    private boolean salvoMode = false;
    private int shotsRemaining = 1;


    //----------------------------------------------------------------------------------------------------------------\\


    // --------- MAIN GAME ITSELF ------- \\
    public static void main(String[] args) {
        new Battleship();
    }
    public Battleship() {
        setTitle("Battleship");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initializeGameState();
        System.out.println("Fleet Created");

        initializeUserInterface();
        System.out.println("Boards Created");
        System.out.println("Panels Created");

        initializeListeners();
        System.out.println("Listeners Created");

        pack(); // Sizes window based on button sizes
        setLocationRelativeTo(null);
        updatePlacementLabel();
        updateShipButtonsEnabled();
        updateBoardPrivacy();
        playBackgroundMusic("background.wav");
        setVisible(true);
    }
    private void initializeGameState(){
        playerOneBoard = new Board(SIZE);
        playerTwoBoard = new Board(SIZE);

        playerOneFleet = createFleet();
        playerTwoFleet = createFleet();
        playerOnePlacedShips = new boolean[playerOneFleet.length];
        playerTwoPlacedShips = new boolean[playerTwoFleet.length];

        inShipPlacementPhase = true;
        playerOnePlacing = true;
        currentShipIndex = 0;
        placeShipHorizontal = true;

        String[] options = {"Normal Mode", "Salvo Mode"};
        int modeChoice = JOptionPane.showOptionDialog(
                this,
                "Choose a game mode: ",
                "Select Game Mode",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        salvoMode = (modeChoice == 1);
    }
    private void initializeUserInterface(){

        turnLabel = new JLabel("BattleShip", SwingConstants.CENTER);
        turnLabel.setFont(new Font("Serif", Font.BOLD, 18));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.add(turnLabel, BorderLayout.CENTER);

        JPanel shipButtonsPanel = new JPanel();
        carrierButton = new JButton("Carrier");
        battleshipButton = new JButton("Battleship");
        cruiserButton = new JButton("Cruiser");
        destroyerButton = new JButton("Destroyer");

        shipButtonsPanel.add(carrierButton);
        shipButtonsPanel.add(battleshipButton);
        shipButtonsPanel.add(cruiserButton);
        shipButtonsPanel.add(destroyerButton);

        topBar.add(shipButtonsPanel, BorderLayout.SOUTH);
        add(topBar, BorderLayout.NORTH);

        randomShipPlacementButton = new JButton("Place All Ships Randomly");
        shipButtonsPanel.add(randomShipPlacementButton, BorderLayout.SOUTH);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2,40,0));

        playerOnePanel = createBoard(true, playerOneBoard); // Player 1 clicks here
        playerTwoPanel = createBoard(false, playerTwoBoard); // Player 2 clicks here

        mainPanel.add(playerOnePanel);
        mainPanel.add(playerTwoPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void initializeListeners(){

        carrierButton.addActionListener(e -> selectShipToPlace(Carrier.class));
        battleshipButton.addActionListener(e -> selectShipToPlace(BattleshipShip.class));
        cruiserButton.addActionListener(e -> selectShipToPlace(Cruiser.class));
        destroyerButton.addActionListener(e -> selectShipToPlace(Destroyer.class));
        randomShipPlacementButton.addActionListener(e -> placeAllShipsRandomlyForCurrentPlayer());

        initializeCellListenersForBoard(playerOnePanel, playerOneBoard);
        initializeCellListenersForBoard(playerTwoPanel, playerTwoBoard);


    }
    private void initializeCellListenersForBoard(JPanel boardPanel, Board model){
        JPanel grid = getGridPanel(boardPanel);

        for(Component comp: grid.getComponents()){

            if(!(comp instanceof CellButton cell)){
                continue;
            }

            //Left CLick
            cell.addActionListener(e -> handleClickCell(cell, model));

            //Right Click + Hover
            cell.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    handleHover(cell, model, true);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    handleHover(cell, model, false);
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if(!inShipPlacementPhase){
                        return;
                    }
                    if(SwingUtilities.isRightMouseButton(e)){
                        placeShipHorizontal = !placeShipHorizontal;
                        JPanel gridPanel = (JPanel) cell.getParent();

                        clearPreviewForBoard(gridPanel);
                        showPreview(cell,model);
                        updateShipButtonsEnabled();
                    }

                }
            });
        }
    }
    private void restartGame() {
        if(backgroundClip != null){
            backgroundClip.stop();
            backgroundClip.close();
        }
        this.dispose();
        new Battleship();
    }


    // ----------- BOARD CREATION ---------- \\
    private Ship[] createFleet(){
        return new Ship[] {
                new Carrier(),
                new BattleshipShip(),
                new Cruiser(),
                new Cruiser(),
                new Destroyer()
        };
    }
    private JPanel createGridPanel(boolean isLeftBoard, Board model){
        JPanel grid = new JPanel();
        grid.setLayout(new GridLayout(SIZE, SIZE));
        grid.setPreferredSize(new Dimension(SIZE * CELL_SIZE, SIZE * CELL_SIZE));

        for (int i = 0; i < SIZE * SIZE; i++) {
            int row = i / SIZE;
            int col = i % SIZE;

            CellButton cell = new CellButton(row, col, isLeftBoard);
            cell.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
            cell.setBackground(new Color(126, 204, 241)); // Light blue
            cell.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            cell.setOpaque(true);
            cell.setFocusPainted(false);

            grid.add(cell);
        }
        return grid;
    }
    private JPanel createBoardLabels(JPanel playerBoard){

        //sets up the letters across the top of each board
        JPanel topPanel = new JPanel(new GridLayout(1, SIZE + 1));
        JLabel corner = new JLabel(" "); //cornor so letters start on cell 1
        corner.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
        topPanel.add(corner);
        for (int col = 0; col < SIZE; col++) {
            JLabel label = new JLabel(String.valueOf((char)('A' + col)), SwingConstants.CENTER);
            label.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
            topPanel.add(label);
        }

        //sets up the numbers along the left side of each board
        JPanel leftPanel = new JPanel(new GridLayout(SIZE, 1));
        for (int row = 0; row < SIZE; row++) {
            JLabel label = new JLabel(String.valueOf(row + 1), SwingConstants.CENTER);
            label.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
            leftPanel.add(label);
        }

        //sets up the main board, which contains each player's board
        JPanel fullPlayerPanel = new JPanel(new BorderLayout());
        fullPlayerPanel.add(topPanel, BorderLayout.NORTH);
        fullPlayerPanel.add(leftPanel, BorderLayout.WEST);
        fullPlayerPanel.add(playerBoard, BorderLayout.CENTER);

        return fullPlayerPanel;
    }
    private JPanel createBoard(boolean isLeftBoard, Board model) {
        JPanel boardPanel = createGridPanel(isLeftBoard, model);
        JPanel boardWithLabels = createBoardLabels(boardPanel);
        return boardWithLabels;
    }


    // ---------- SHIP PLACEMENT LOGIC -------- \\
    private void selectShipToPlace(Class<? extends Ship> shipClass){
        if(!inShipPlacementPhase){
            return;
        }

        Ship[] fleet = playerOnePlacing ? playerOneFleet : playerTwoFleet;
        boolean[] placedShips = playerOnePlacing ? playerOnePlacedShips : playerTwoPlacedShips;

        if(fleet == null){
            return;
        }

        for(int i = 0; i < fleet.length; i++){
            if(!placedShips[i] && shipClass.isInstance(fleet[i])){
                currentShipIndex = i;
                updatePlacementLabel();
                return;
            }
        }

        JOptionPane.showMessageDialog(this, "All " + shipClass.getSimpleName() +
                "s are already placed.", "No ships left", JOptionPane.INFORMATION_MESSAGE );
    }
    private void placeAllShipsRandomlyForCurrentPlayer(){
        if(!inShipPlacementPhase){
            return;
        }
        Board model = playerOnePlacing ? playerOneBoard : playerTwoBoard;
        JPanel playerPanel = playerOnePlacing ? playerOnePanel : playerTwoPanel;
        Ship[] fleet = playerOnePlacing ? playerOneFleet : playerTwoFleet;
        boolean[] placedShips = playerOnePlacing ?  playerOnePlacedShips : playerTwoPlacedShips;

        for (boolean placed : placedShips) {
            if (placed) {
                JOptionPane.showMessageDialog(
                        this,
                        "Random placement is only available before placing any ships.",
                        "Cannot Randomize",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }
        }

        model.placeShipsRandomly(fleet);
        for(int i = 0; i < placedShips.length; i++){
            placedShips[i] = true;
        }

        JPanel grid = getGridPanel(playerPanel);
        Component[] comps = grid.getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (!(comps[i] instanceof CellButton btn)) continue;

            int row = i / SIZE;
            int col = i % SIZE;

            // Show ships directly from the model
            if (model.isShip(row, col)) {
                btn.setState(CellButton.ship);
            } else {
                int cellVal = model.getCell(row, col);
                if (cellVal == Board.HIT) {
                    btn.setState(CellButton.hit);
                } else if (cellVal == Board.MISS) {
                    btn.setState(CellButton.miss);
                } else {
                    btn.setState(CellButton.none);
                }
            }
        }
        clearPreviewForBoard(grid);
        updateShipButtonsEnabled();
        nextPlacement();
    }
    private void updateShipButtonsEnabled() {
        boolean carrierLeft = false;
        boolean battleshipLeft = false;
        boolean cruiserLeft = false;
        boolean destroyerLeft = false;

        boolean anyShipsPlaced = false;
        boolean anyShipsUnplaced = false;

        if(inShipPlacementPhase){
            Ship[] fleet = playerOnePlacing ? playerOneFleet : playerTwoFleet;
            boolean[] placedShips = playerOnePlacing ? playerOnePlacedShips : playerTwoPlacedShips;

            if(fleet != null && placedShips != null){
                for(int i = 0; i < fleet.length; i++){
                    Ship ship = fleet[i];

                    if(placedShips[i]){
                        anyShipsPlaced = true;
                        continue;
                    }
                    anyShipsUnplaced = true;

                    if(ship instanceof Carrier){
                        carrierLeft = true;
                    } else if (ship instanceof BattleshipShip) {
                        battleshipLeft = true;
                    }
                    else if(ship instanceof Cruiser){
                        cruiserLeft = true;
                    }
                    else if(ship instanceof Destroyer){
                        destroyerLeft = true;
                    }
                }
            }
        }
        if(carrierButton != null){
            carrierButton.setEnabled(carrierLeft);
        }
        if(battleshipButton != null){
            battleshipButton.setEnabled(battleshipLeft);
        }
        if(cruiserButton != null){
            cruiserButton.setEnabled(cruiserLeft);
        }
        if(destroyerButton != null){
            destroyerButton.setEnabled(destroyerLeft);
        }
        if(randomShipPlacementButton != null) {
            boolean canPlaceRandom = inShipPlacementPhase && !anyShipsPlaced && anyShipsUnplaced;
            randomShipPlacementButton.setEnabled(canPlaceRandom);
        }
    }
    private void updatePlacementLabel(){
        if(!inShipPlacementPhase){
            if(salvoMode) {
                String player = playerOnesTurn ? "Player 1" : "Player 2";
                turnLabel.setText(player + "'s Turn - Salvo: " + shotsRemaining + " Shots Remaining!");
            }
            else {
                if(playerOnesTurn)
                    turnLabel.setText("Player 1's Turn");
                else
                    turnLabel.setText("Player 2's Turn");
            }
            return;
        }

        Ship ship = getCurrentShip();
        if(ship == null){
            turnLabel.setText("Placing Ships");
            return;
        }

        String playerText = playerOnePlacing ? "Player 2" : "Player 1";

        turnLabel.setText(playerText + ": Place your " + ship.getName() + " (size " + ship.getLength() + ") - " +
                        " Left Click to Place, Right Click to Rotate");
    }
    private void handlePlacementClick(CellButton cell, Board model){
        if(playerOnePlacing && model != playerOneBoard){
            return;
        }
        if(!playerOnePlacing && model == playerOneBoard){
            return;
        }
        Ship ship = getCurrentShip();
        if(ship == null){
            return;
        }

        int row = cell.row;
        int col = cell.col;

        if(!model.canPlaceShip(row, col, ship.getLength(), placeShipHorizontal)) {
            JOptionPane.showMessageDialog(this, "You can't place this ship");
            return;
        }
        boolean placed = model.placeShipOnBoard(ship, row, col, placeShipHorizontal);
        if(!placed){
            return;
        }

        JPanel playerPanel = (model == playerOneBoard) ? playerOnePanel : playerTwoPanel;
        JPanel grid = getGridPanel(playerPanel);

        for(int i = 0; i < ship.getLength(); i++){
            int r = placeShipHorizontal ? row: row + i;
            int c = placeShipHorizontal ? col + i: col;

            CellButton btn = getCellButtonAt(grid, r, c);
            if(btn != null){
                btn.setState(CellButton.ship);
            }
        }

        boolean[] placedShips = playerOnePlacing ? playerOnePlacedShips : playerTwoPlacedShips;
        placedShips[currentShipIndex] = true;
        updatePlacementLabel();

        clearPreviewForBoard(grid);
        nextPlacement();
    }
    private void nextPlacement(){
        Ship[] currentFleet = playerOnePlacing ?  playerOneFleet : playerTwoFleet;
        boolean[] placedShips = playerOnePlacing ? playerOnePlacedShips : playerTwoPlacedShips;

        boolean shipsLeftToPlace = false;
        int firstUnplacedShipIndex = -1;
        if(currentFleet != null && placedShips != null){
            for(int i = 0; i < currentFleet.length; i++){
                if(!placedShips[i]){
                    shipsLeftToPlace = true;
                    firstUnplacedShipIndex = i;
                }
            }
        }

        if(shipsLeftToPlace){
            currentShipIndex = firstUnplacedShipIndex;
            updatePlacementLabel();
            updateShipButtonsEnabled();
            return;
        }

        if(playerOnePlacing){
            playerOnePlacing = false;
            currentShipIndex = 0;
            placeShipHorizontal = true;
            JOptionPane.showMessageDialog(this, "Now Player 1's turn to place ships");
            updateBoardPrivacy();
            updateShipButtonsEnabled();
        }
        else{
            inShipPlacementPhase = false;
            playerOnePlacing = true;
            placeShipHorizontal = true;

            if(carrierButton != null){
                carrierButton.setEnabled(false);
            }
            if(battleshipButton != null){
                battleshipButton.setEnabled(false);
            }
            if(cruiserButton != null){
                cruiserButton.setEnabled(false);
            }
            if(destroyerButton != null){
                destroyerButton.setEnabled(false);
            }
            JOptionPane.showMessageDialog(this, "All Ships Placed. Game Start");

            if(salvoMode) {
                shotsRemaining = playerTwoBoard.getUnsunkCount();
                turnLabel.setText("Player 1's Turn - Salvo: " + shotsRemaining + " Shots Remaining!");
            }

        }
        updateBoardPrivacy();
        updatePlacementLabel();

    }
    private void handleHover(CellButton cell, Board model, boolean entered){
        if(!inShipPlacementPhase){
            return;
        }

        if(playerOnePlacing && model != playerOneBoard){
            return;
        }
        if(!playerOnePlacing && model == playerOneBoard){
            return;
        }

        JPanel gridPanel = (JPanel) cell.getParent();
        clearPreviewForBoard(gridPanel);
        if(!entered) {
            return;
        }
        showPreview(cell, model);
    }
    private void clearPreviewForBoard(JPanel gridPanel){
        for(Component comp : gridPanel.getComponents()){
            if(comp instanceof CellButton btn){
                int state = btn.getState();
                if(state == CellButton.hoverGood || state == CellButton.hoverBad){
                    btn.setState(CellButton.none);
                }
            }
        }
    }
    private void showPreview(CellButton cell, Board model){
        Ship ship = getCurrentShip();
        if(ship == null){
            return;
        }
        int length = ship.getLength();
        int row = cell.row;
        int col = cell.col;

        boolean canPlace = model.canPlaceShip(row, col, length, placeShipHorizontal);
        JPanel gridPanel = (JPanel) cell.getParent();


        for(int i = 0; i < length; i++){
            int r = placeShipHorizontal ? row: row + i;
            int c = placeShipHorizontal ? col + i: col;

            if(r < 0 || c < 0 || r >= SIZE || c >= SIZE) {
                continue;
            }

            CellButton btn = getCellButtonAt(gridPanel, r, c);
            if(btn != null){
                int existingState = btn.getState();
                if(existingState == CellButton.ship){
                    continue;
                }
                btn.setState(canPlace ? CellButton.hoverGood : CellButton.hoverBad);
            }
        }
    }
    private CellButton getCellButtonAt(JPanel grid, int row, int col){
        int index = row * SIZE + col;
        if(index < 0 || index >= grid.getComponentCount()) {
            return null;
        }
        Component comp = grid.getComponent(index);
        if(comp instanceof CellButton){
            return (CellButton)comp;
        }
        return null;
    }
    private Ship getCurrentShip(){
        Ship [] currentFleet = playerOnePlacing ?  playerOneFleet : playerTwoFleet;
        if(currentShipIndex < 0 ||  currentShipIndex >= currentFleet.length){
            return null;
        }
        return currentFleet[currentShipIndex];
    }


    // -------- CLICKING LOGIC DURING GAME -------- \\
    private void handleClickCell(CellButton cell, Board model){

        int row = cell.row;
        int col = cell.col;

        if(inShipPlacementPhase){
            handlePlacementClick(cell, model);
            return;
        }

        //If location has already been shot at, do nothing
        if(model.isHit(row, col))
            return;

        //If its is between turns, do nothing
        if(turnLocked)
            return;

        // Player 1 can only click LEFT board
        if(playerOnesTurn && !cell.isLeftBoard)
            return;

        // Player 2 can only click RIGHT board
        if(!playerOnesTurn && cell.isLeftBoard)
            return;

        //hit case
        if(model.isShip(row, col)) {
            model.markHit(row, col);
            cell.setState(CellButton.hit);
            playSound("hit.wav");

            //what kind of ship got hit
            Ship hitShip = model.getShipAt(row, col);
            boolean wasHit = hitShip.shipHasBeenHit(row, col);

            //if no more ships left to hit
            if(model.allShipsSunk()){
                String winner = playerOnesTurn ? "Player 1" : "Player 2";

                //Which player wins
                int result = JOptionPane.showConfirmDialog(
                        Battleship.this,
                        winner + " Wins!\nPlay again?",
                        "Game Over",
                        JOptionPane.YES_NO_OPTION
                );

                if(result==JOptionPane.YES_OPTION) {
                    restartGame();
                }
                else {
                    System.exit(0);
                }
            }


            //keep turn if hit ship
            /* turnLabel.setText(
                    " Hit! Shoot again!"
            ); */
            if(!salvoMode)
                turnLabel.setText("Hit! Shoot Again!");
            else {
                shotsRemaining--;
                if(hitShip.isSunk()) {
                    turnLabel.setText("You Sank My " + hitShip.getName() + "!");
                    if(cell.isLeftBoard)
                        markSurroundingCells(model, playerOnePanel, hitShip);
                    else
                        markSurroundingCells(model, playerTwoPanel, hitShip);
                }

                if(shotsRemaining <= 0) {
                    endTurn("Salvo Turn Over! ");
                    return;
                }
                else {
                    String player = playerOnesTurn ? "Player 1" : "Player 2";
                    turnLabel.setText(player + "'s Turn - Salvo: " + shotsRemaining + " shots left!");
                }
                
            }

            //display message when ship is sank
            if(hitShip.isSunk()) {
                turnLabel.setText(
                        "You sank my " + hitShip.getName() + "!"
                );
                if(cell.isLeftBoard)
                    markSurroundingCells(model, playerOnePanel, hitShip);
                else
                    markSurroundingCells(model, playerTwoPanel, hitShip);
            }

            return;
        }

        //normal miss case
        model.markMiss(row, col);
        cell.setState(CellButton.miss);
        playSound("miss.wav");
        updateBoardPrivacy();
        if(!salvoMode) {
            endTurn("You Missed! ");
            return;
        }

        //salvo miss case
        shotsRemaining--;
        if(shotsRemaining <= 0) {
            endTurn("Salvo turn over! ");
        }
        else {
            String player = playerOnesTurn ? "Player 1" : "Player 2";
            turnLabel.setText(player + "'s Turn - Salvo: " + shotsRemaining + " shots left!");
        }
        return;

    }
    private void markSurroundingCells(Board model, JPanel boardPanel, Ship ship) {
        int size = SIZE;
        int row = ship.getStartRow();
        int col = ship.getStartCol();
        int length = ship.getLength();
        boolean horizontal = ship.isHorizontal();

        Component[] components = getGridPanel(boardPanel).getComponents();

        // helper
        java.util.function.BiConsumer<Integer, Integer> updateCell = (r, c) -> {
            if(r < 0 || c < 0 || r >= size || c >= size)
                return;

            if(model.getCell(r, c) == Board.WATER) {
                model.setCell(r, c, Board.MISS);
                int index = r * size + c;
                CellButton btn = (CellButton) components[index];
                btn.setState(CellButton.miss);
            }
        };

        if (horizontal) {
            for(int rr = row - 1; rr <= row + 1; rr++) {
                for(int cc = col - 1; cc <= col + length; cc++) {
                    updateCell.accept(rr, cc);
                }
            }
        }
        else {
            for(int rr = row - 1; rr <= row + length; rr++) {
                for(int cc = col - 1; cc <= col + 1; cc++) {
                    updateCell.accept(rr, cc);
                }
            }
        }
    }


    // ---------- TURN LOGIC ---------- \\
    private void endTurn(String message) {

        //lock the boards and change ship visibility on both boards
        turnLocked = true;
        setBoardsEnabled(false);
        countdown = TURN_DELAY_SECONDS;

        turnLabel.setText(message + "Next turn in: " + countdown);
        updateBoardPrivacy();

        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                countdown--;

                if(countdown > 0) {
                    turnLabel.setText(message + "Next turn in: " + countdown);
                }
                else {
                    countdownTimer.stop();
                    switchPlayerTurn();
                }
            }
        });
        countdownTimer.start();
    }
    private void switchPlayerTurn() {
        playerOnesTurn = !playerOnesTurn;
        turnLocked = false;
        setBoardsEnabled(true);
        updateBoardPrivacy();
        if(salvoMode) {
            Board enemyBoard = playerOnesTurn ? playerTwoBoard : playerOneBoard;
            shotsRemaining = enemyBoard.getUnsunkCount();
        }

        if(salvoMode) {
            String player = playerOnesTurn ? "Player 1" : "Player 2";
            turnLabel.setText(player + "'s Turn - Salvo: " + shotsRemaining + " shots Remaining!");
        }
        else {
            if(playerOnesTurn)
                turnLabel.setText("Player 1's Turn");
            else
                turnLabel.setText("Player 2's Turn");
        }
    }
    private void setBoardsEnabled(boolean enabled) {
        for (Component c : playerOnePanel.getComponents()) {
            c.setEnabled(enabled);
        }
        for (Component c : playerTwoPanel.getComponents()) {
            c.setEnabled(enabled);
        }
    }
    private void updateBoardPrivacy() {
        updatePrivacyEachTurn(playerTwoPanel, playerTwoBoard, true);
        updatePrivacyEachTurn(playerOnePanel, playerOneBoard, false);
    }
    private void updatePrivacyEachTurn(JPanel playerBoard, Board model, boolean isLeftBoard) {
        JPanel grid = getGridPanel(playerBoard);
        Component[] comps = grid.getComponents();

        for(int i = 0; i < comps.length; i++) {
            CellButton btn = (CellButton) comps[i];
            int row = i / SIZE;
            int col = i % SIZE;

            //Always show the hits and misses, no matter the player turn
            if(model.getCell(row, col) == Board.HIT) {
                btn.setState(CellButton.hit);
                continue;
            }
            if(model.getCell(row, col) == Board.MISS) {
               btn.setState(CellButton.miss);
               continue;
           }

            boolean isShip = model.isShip(row, col);

            //hide everything during countdown
            if(turnLocked) {
                btn.setState(CellButton.none);
                continue;
            }

            //Player 1's Turn, so hide Player 2's ships
            if(playerOnesTurn) {
                if(isLeftBoard) {
                    //show player 1s ships
                    btn.setState(isShip ? CellButton.ship : CellButton.none);
                }
                else {
                    //hide other ships
                    btn.setState(isShip ? CellButton.hidden : CellButton.none);
                }
            }
            //Player 2's Turn, so hide Player 1's ships
            else {
                if(!isLeftBoard) {
                   //show player 2s ships
                   btn.setState(isShip ? CellButton.ship : CellButton.none);
                }
                else {
                    //hide other ships
                    btn.setState(isShip ? CellButton.hidden : CellButton.none);
                }
        }
        }
    }
    private JPanel getGridPanel(JPanel outerBoard){
        return (JPanel)((BorderLayout) outerBoard.getLayout())
                .getLayoutComponent(BorderLayout.CENTER);
    }


    //--------SOUND FX----------//
    private void playSound(String fileName) {
        try {
            java.net.URL url = getClass().getResource(fileName);
            javax.sound.sampled.AudioInputStream audioIn = javax.sound.sampled.AudioSystem.getAudioInputStream(url);
            javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } 
        catch (Exception e) {
           e.printStackTrace();
        }
    }

    private Clip backgroundClip;

    private void playBackgroundMusic(String fileName) {
        try {
            if(backgroundClip != null && backgroundClip.isRunning()) {
              backgroundClip.stop();
                backgroundClip.close();
            }

            java.net.URL url = getClass().getResource(fileName);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);

            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(audioIn);
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundClip.start();
        } 
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}