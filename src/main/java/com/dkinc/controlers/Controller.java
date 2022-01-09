package com.dkinc.controlers;


import com.dkinc.rooms.Room;
import com.dkinc.rooms.User;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

@RestController
public class Controller {

    private HashMap<String,Room> rooms = new HashMap<>();
    private ArrayList<Integer> cards = new ArrayList<>();
    Random random = new Random();
    boolean sync = false;

    User bank;
    User player;

    //ПРИ СОЗДАНИИИ КОНТРОЛЛЕРА ИНИЦАЛИЗИРУЕТСЯ МАССИВ КАРТ
    public Controller(){
        initCards();
    }
    //КАЖДЫЙ РАУНД МАССИВ БУДЕТ ИНИЦИАЛИЗИРОВАТСЬЯ ЗАНОВО
    public void initCards(){
        for(int i = 1; i <= 36; i++){
            cards.add(i);
        }
    }
    //МЕТОД ДЛЯ ТЕСТА СИНХРОНИЗАЦИИ
    @RequestMapping("/testsync")
    public String testSync(@RequestParam @Nullable Integer command){
        if(command != null && command == 1){
            sync = true;
        }
        if(sync){
            return "sync complete";
        }
        else{
            return "sync not complete";
        }
    }
    //ПРИ СОЗДАНИИ КОМАНТЫ БУДЕТ 2 ВАРИНТА ГЕНЕРАЦИИ ИМЕНИ
    //ЛИБО АВТОГЕНЕРАЦИЯ ЛИБО СВОЕ ИМЯ КОМНАТЫ
    //ВО ВРЕМЯ СОЗДАНИЯ КОМНАТЫ БУДЕТ ВЫЗЫВАТЬСЯ ВНУТРЕННИЙ МЕТОД СОЗДАНИЯ КОМНАТЫ
    @GetMapping("/createRoom")
    public String createRoomDESIGN(@RequestParam String nameU1,@RequestParam @Nullable String nameRoom){
        nameRoom = nameRoom == null ? createNameRoom() : nameRoom;
        rooms.put(nameRoom,createRoom(nameU1,nameRoom));
        return rooms.entrySet().toString();
    }
    //МЕТОД ДЛЯ ПРИСОЕДИНЕНИЯ ИГРОКОВ
    //ИДЕТ ПРОВЕРКА НА ТО, ЕСТЬ ЛИ ВТОРОЙ ИГРОК
    //ЕСЛИ НЕТ, ТО ПРИСВАЕИВАЕМ ИГРОКУ 2 - ВТОРОГО ИГРОКА
    //КОМНАТА МЕНЯЕТСЯ, ПОЭТОМУ МЕНЯЕМ ЗНАЧЕНИЕ КОМНТАЫ В МАССИВЕ ДЛЯ АКТУЛАЛЬНЙО ИНФОРМАЦИИ О КОНМАТАХ
    //ИДЕТ ПЕРЕАДЕРСАЦИЯ НА МЕТОД PLAY
    @RequestMapping(
            value = "/connect",
            method = RequestMethod.GET)
    public void connect(@RequestParam String idRoom, @RequestParam String nameUser,HttpServletResponse httpServletResponse) throws IOException {
        Room room = rooms.get(idRoom);
        if (room.getU2() == null){
            room.setU2(new User(nameUser,!room.getU1().isBank(), !room.getU1().isCanGetCard()));
            rooms.replace(idRoom,room);
        }
        httpServletResponse.sendRedirect("/play?idRoom=" + idRoom);
    }
    //*ВНУТРЕННИЙ МЕТОД СОЗДАНИЯ КОМНАТЫ*
    //ПОСЛЕ СОЗДАНИЯ КОМНТАЫ, СОЗДАЕТСЯ ПОЛЬЗВАТЕЛЬ
    //ВТОРОЙ ПОЛЬЗОВАТЕЛЬ БУДЕТ NULL
    //ПЕРВОМУ ПОЛЬЗОВАТЕЛЮ ПРИСВАИВАЕТСЯ ЗНАЧЕНИЕ БАНКА
    //БАНК = НЕ МОЖЕТ БРАТЬ КАРТЫ
    //ВОЗВРАЩАЕТСЯ СОЗДАННАЯ КОМНАТА И ДОБАВЛЯЕТСЯ В МАССИВ ВСЕХ КОМНАТ
    public Room createRoom(String nameU1, @Nullable String nameRoom){
        boolean fBank = random.nextBoolean();
        User u1 = new User(nameU1,fBank,!fBank);
        User u2 = null;
        String idRoom = nameRoom == null ? createNameRoom() : nameRoom;
        Room room = new Room(u1,u2,idRoom);
        return room;
    }
    //СОЗДАНИЕ РАНДОМНОГО ИМЕНИ КОМНАТЫ, ЕСЛИ ОНО НЕ БЫЛО ЗАДАНО ЗАРАНЕЕ
    private String createNameRoom(){
        int leftLimit = 97;
        int rightLimit = 122;
        int targetStringLength = 10;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }
    //МЕТОД ДЛЯ НАЧАЛА ИГРЫ И ОПРЕДЕЛЕНИЯ БАНКА И ИГРОКА
    @GetMapping("/play")
    public void play(String idRoom){
        Room room = rooms.get(idRoom);
        if(room.getU1() != null && room.getU2() != null){
            bank = room.getU1().isBank() ? room.getU1() : room.getU2();
            player = room.getU2().isBank() ? room.getU1() : room.getU2();
        }
    }
    //ПОДАЕТСЯ КОМАНДА ДЛЯ ПОЛУЧЕНИЯ КАРТ
    //1 - ВЗЯТЬ КАРТУ
    //2 - КОГДА 2 ИГРОКА НАЖАЛИ HOLD, ТО НАЧИНАЕТСЯ ПРОВЕРКА ПОБЕДИТЕЛЯ
    //0 - КОГДА ИГРОК НАЖАЛ HOLD И БАНК НАЧИНАЕТ БРАТЬ КАРТЫ СЕБЕ
    @GetMapping("/getCard")
    public void playComand(@RequestParam Integer command, HttpServletResponse httpServletResponse) throws IOException {
        if (command == 1) {
            if(player.isCanGetCard()) {
                player.setScore(cards.get(random.nextInt(36)) + player.getScore());
            }else {
                bank.setScore(cards.get(random.nextInt(36)) + bank.getScore());
            }
            checkWin(httpServletResponse);
        }
        if(command == 2){
            httpServletResponse.sendRedirect("/endGame");
        }
        if(command == 0){
            player.setCanGetCard(false);
            bank.setCanGetCard(true);
        }
    }
    //ПРОВЕРКА НА ПОБЕДИТЕЛЯ ИДЕТ КАЖДУЮ НОВУЮ КАРТУ
    //ЕСЛИ ИГРОК НАБРАЛ 21, ТО ОН ВЫИГРАЛ (БАНК АНАЛОГИЧНО)
    public void checkWin(HttpServletResponse httpServletResponse) throws IOException {
        if(player.getScore() == 21){
            httpServletResponse.sendRedirect("/endGame");
        }
        if(bank.getScore() == 21){
            httpServletResponse.sendRedirect("/endGame");
        }
    }
    //ПРИ ОТКЛЮЧЕНИИ ИГРОКА КОМНАТА ПРОВЕРЯЕТСЯ НА НАЧИЛИЕ В НЕЙ ИГРОКОВ
    //ЕСЛИ ИГРОКОВ НЕТ, ТО КОМНАТА УДАЛЯЕТСЯ
    @GetMapping("disconnect")
    public void disconnect(Long id){
   //TODO     if()
    }
    //ЕСЛИ НИКТО ИЗ ИГРОКОВ НЕ НАБРАЛ 21, ТО ИДЕТ ПРОВЕРКА НА ПОБЕДИТЕЛЯ ПО НАБРАННЫМ ОЧКАМ
    //ОТВЕТОМ ЯВЛЯЕТСЯ КОМАНДА, КОТОРАЯ БУДЕТ РЕШАТЬ, ЧТО ВЫСВЕТИТЬ КАЖДОМУ ИГРОКУ НА ЭКАН(WIN, LOSE)
    @GetMapping("/endGame")
    public Integer endGame(){
        if(player.getScore() > bank.getScore() || player.getScore() == 21){
            return 1;
        }else if(bank.getScore() > player.getScore() || bank.getScore() == 21) {
            return 2;
        }else{
            return 0;
        }
    }
    //УДАЛЕНИЕ КОМНАТЫ ПО ИМЕНИ
    @GetMapping("/deleteRoom")
    public void deleteRoom(@RequestParam String nameRoom){
        rooms.remove(nameRoom);
    }
    //МЕТОД ДЛЯ ВЫВОДА СПИСКА КОМНАТ
    @GetMapping("/listRoom")
    public HashMap<String, Room> listRoom(){
        return rooms;
    }
    //МЕТОД ДЛЯ ВЫВОДА ИНФОРМАЦИИ ОБ ИГРОКЕ
    //В АРГУМЕНТЫ ПОСТУПАЕТ КОМАНДА ДЛЯ ПРОВЕРКИ КАКУЮ ИМЕННО ИНФОРМАЦИЮ ОТПРАВЛЯТЬ (ОБ ИГРКОЕ ИЛИ БАНКЕ)
    @GetMapping("/getInfo")
    public Room getInfoRoom(String idRoom){
        return rooms.get(idRoom);
    }
}
