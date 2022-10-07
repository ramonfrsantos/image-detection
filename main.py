import PySimpleGUI as sg
import base64
import requests
from PIL import Image
from io import BytesIO
import os.path

# Definição das duas colunas
column_left = [ # Coluna da esquerda
    [
        sg.Text("Image Folder"),
        sg.In(size=(25, 1), enable_events=True, key="-FOLDER-"),
        sg.FolderBrowse(),
    ],
    [
        sg.Text("Lista de imagens:"),
    ],
    [
        sg.Listbox(
            values=[], enable_events=True, size=(40, 20), key="-FILE LIST-"
        )
    ],
]
column_right = [ # Coluna da direita
    [sg.Text("Choose an image from the list on the left:")],
    [sg.Text(size=(40, 1), key="-TOUT-", text_color="yellow")],
    [sg.Graph((400, 400), (0, 0), (400, 400), key="-IMAGE-", drag_submits=True, enable_events=True)],
    [sg.Button("Recortar", key="-BTN_CROP-"),sg.Button("Identificar", key="-BTN_MATCH-")]
]

# ----- Layout completo -----
layout = [
    [
        sg.Column(column_left),
        sg.VSeperator(),
        sg.Column(column_right),
    ]
]

window = sg.Window("Image Viewer", layout)
x0, y0 = None, None # coordenadas xy do primeiro clique do mouse
x1, y1 = None, None # coordenadas xy do segundo clique do mouse
x0s, y0s, x1s, y1s = None, None, None, None # coordenadas xy para recortar
colors = ['blue', 'white'] # Cores da borda da área selecionada para recortar
index = False # Índice da cor da borda atual
crop = None # Área de seleção do mouse 
cropSave = None # Área de seleção do mouse
filename = None # Nome do arquivo
URL = "http://localhost:8001/image-analysis" # URL da API de análise de imagens

def updateFileList(): # Método para atualizar a lista de arquivos de um diretório
    folder = values["-FOLDER-"]
    try:
        # Listar os arquivos
        file_list = os.listdir(folder)
    except:
        file_list = []

    fnames = [
        f
        for f in file_list
        if os.path.isfile(os.path.join(folder, f))
        and f.lower().endswith((".png", ".gif"))
    ]
    window["-FILE LIST-"].update(fnames) # Atribuir os nomes dos arquivos ao conteúdo da lista de arquivos.

# Loop do programa
while True:
    event, values = window.read()
    if event == "Exit" or event == sg.WIN_CLOSED:
        break
    if event == "-FOLDER-": # Se uma pasta foi selecionada, atualizar a lista de arquivos
        updateFileList()
    elif event == "-FILE LIST-":  # Se um arquivo foi selecionado, 
        try:
            filename = os.path.join(
                values["-FOLDER-"], values["-FILE LIST-"][0]
            )
            window["-TOUT-"].update(filename)
            window["-IMAGE-"].erase()
            window["-IMAGE-"].draw_image(filename=filename, location=(0,400))

        except:
            pass
    elif event == "-BTN_CROP-": # Clicado no botão de recortar
        # Utiliza as coordenadas da área selecionada com o mouse para salvar uma região da imagem.
        if cropSave == None:
            window["-TOUT-"].update("Selecione uma área na imagem abaixo para recortar.")
        elif filename == None:
            window["-TOUT-"].update("Selecione uma imagem.")
        else:
            try:
                im = Image.open(filename)
                # O componente Graph possui tamanho de 400x400 e possui eixo Y invertido (onde Y aumenta
                # quanto mais acima a coordenada é), por isso subtraimos a posição Y do mouse por 400
                cropImage = im.crop((x0s, 400-y1s, x1s, 400-y0s))
                cropImageName, cropImageExt = os.path.splitext(filename)
                cropFilename = cropImageName + "_crop" + cropImageExt
                cropImage.save(cropFilename)
                window["-TOUT-"].update("\"" + cropFilename + "\" salvo!")
                updateFileList()
            except:
                window["-TOUT-"].update("Ocorreu um erro, tente novamente.")
            
    elif event == "-BTN_MATCH-": # Clicado no botão de Identificar
        with open(filename, "rb") as image_file:
            imageb64 = base64.b64encode(image_file.read()) # Lê o conteúdo da imagem em base64
        params = {# Parâmetros a serem passados para a API
            "base64": imageb64.decode("utf-8"),
            "formato": os.path.splitext(filename)[1]
        }
        request = requests.post(url = URL, json = params) # Realiza a requisição
        response = request.json() # Obtém o JSON da resposta da requisição
        im = Image.open(BytesIO(base64.b64decode(response["base64Convertido"]))) # Carrega o conteúdo da requisição
        responseImageName, responseImageExt = os.path.splitext(filename)
        responseFilename = responseImageName + "_resultado" + responseImageExt
        im.save(responseFilename) # Salva a imagem da resposta da requisição
        window["-IMAGE-"].erase() 
        window["-IMAGE-"].draw_image(filename=responseFilename, location=(0,400)) # Exibe a imagem na tela
    elif event in ('-IMAGE-', '-IMAGE-+UP'): # Arrastando o mouse
        if (x0, y0) == (None, None):
            x0, y0 = values['-IMAGE-']
            window["-IMAGE-"].delete_figure(cropSave)
            cropSave = None
        x1, y1 = values['-IMAGE-']
        if event == '-IMAGE-+UP': # Soltando o botão direito do mouse
            x0s, y0s = x0, y0
            x1s, y1s = x1, y1
            if x0s > x1s: # x0s e y0s serão utilizados para recortar a imagem, mas para isso é necessário que
                # x0 e y0 sejam maior do que x1 e y1
                x0s = x1
                x1s = x0
            if y0s > y1s:
                y0s = y1
                y1s = y0
            cropSave = window["-IMAGE-"].draw_rectangle((x0, y0), (x1, y1), line_color="blue")
            x0, y0 = None, None
    if crop: # Deletar o retângulo anterior e desenhar um noovo
        window["-IMAGE-"].delete_figure(crop)
    if None not in (x0, y0, x1, y1): # Se qualquer uma das 4 coordenadas não for vazia,
        # desenhar o retângulo
        crop = window["-IMAGE-"].draw_rectangle((x0, y0), (x1, y1), line_color=colors[index])
        index = not index # Alternar entre as azul e branco ao desenhar o retângulo

window.close()
