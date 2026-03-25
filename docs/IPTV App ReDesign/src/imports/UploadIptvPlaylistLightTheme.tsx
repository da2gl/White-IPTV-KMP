import svgPaths from "./svg-iygcnbfh2v";

function Wrapper({ children }: React.PropsWithChildren<{}>) {
  return (
    <div className="flex flex-row items-center justify-center min-w-[inherit] overflow-clip rounded-[inherit] size-full">
      <div className="content-stretch flex items-center justify-center min-w-[inherit] px-[20px] relative size-full">{children}</div>
    </div>
  );
}

function Separator() {
  return (
    <div className="flex-[1_0_0] h-px min-h-px min-w-px relative">
      <div aria-hidden="true" className="absolute border-[#e2e8f0] border-solid border-t inset-0 pointer-events-none" />
    </div>
  );
}

export default function UploadIptvPlaylistLightTheme() {
  return (
    <div className="bg-[#f6f7f8] content-stretch flex flex-col items-start relative size-full" data-name="Upload IPTV Playlist (Light Theme)">
      <div className="content-stretch flex flex-col items-center justify-center min-h-[884px] overflow-clip pb-[244.5px] relative shrink-0 w-full" data-name="Container">
        <div className="h-[639.5px] max-w-[448px] relative shrink-0 w-full" data-name="Container">
          <div className="-translate-x-1/2 -translate-y-1/2 absolute content-stretch flex gap-[12px] items-center left-1/2 pb-[32px] top-[calc(50%-231.75px)]" data-name="App Logo/Name">
            <div className="bg-[#2badee] content-stretch flex items-center justify-center relative rounded-[24px] shrink-0 size-[48px]" data-name="Background">
              <div className="h-[22.5px] relative shrink-0 w-[25px]" data-name="Container">
                <svg className="absolute block size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 25 22.5">
                  <g id="Container">
                    <path d={svgPaths.p27c75580} fill="var(--fill-0, white)" id="Icon" />
                  </g>
                </svg>
              </div>
            </div>
            <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Heading 1">
              <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold h-[40px] justify-center leading-[0] not-italic relative shrink-0 text-[#0f172a] text-[32px] tracking-[-0.64px] w-[112.06px]">
                <p className="leading-[40px]">YourTV</p>
              </div>
            </div>
          </div>
          <div className="-translate-y-1/2 absolute content-stretch flex flex-col items-start left-[24px] right-[24px] top-[calc(50%-146px)]" data-name="Header & Subheader">
            <div className="content-stretch flex flex-col items-center pb-[8px] pt-[20px] relative shrink-0 w-full" data-name="Heading 2">
              <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold h-[28px] justify-center leading-[0] not-italic relative shrink-0 text-[#0f172a] text-[22px] text-center tracking-[-0.33px] w-[262.03px]">
                <p className="leading-[27.5px]">Upload your IPTV playlist</p>
              </div>
            </div>
            <div className="content-stretch flex flex-col items-center pb-[12px] relative shrink-0 w-full" data-name="Container">
              <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#475569] text-[16px] text-center w-[335.95px]">
                <p className="leading-[24px]">Enter a link or choose a file to start watching</p>
              </div>
            </div>
          </div>
          <div className="-translate-y-1/2 absolute content-stretch flex flex-col gap-[16px] items-center left-[24px] right-[24px] top-[calc(50%+27.75px)]" data-name="Input Form">
            <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="Text Input Field → Label">
              <div className="content-stretch flex flex-col items-start pb-[8px] relative shrink-0 w-full" data-name="Container">
                <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium justify-center leading-[0] not-italic relative shrink-0 text-[#1e293b] text-[16px] w-full">
                  <p className="leading-[24px]">Playlist URL (M3U, M3U8)</p>
                </div>
              </div>
              <div className="bg-white h-[52px] relative rounded-[24px] shrink-0 w-full" data-name="Input">
                <div className="overflow-clip relative rounded-[inherit] size-full">
                  <div className="absolute bottom-[16px] content-stretch flex flex-col items-start left-[16px] overflow-clip pr-[242.48px] top-[16px]" data-name="Container">
                    <div className="flex flex-col font-['Inter:Regular',sans-serif] font-normal h-[20px] justify-center leading-[0] not-italic relative shrink-0 text-[#94a3b8] text-[16px] w-[67.52px]">
                      <p className="leading-[normal]">{`https://...`}</p>
                    </div>
                  </div>
                  <div className="absolute bottom-[16px] left-[16px] top-[16px] w-[310px]" data-name="Container" />
                </div>
                <div aria-hidden="true" className="absolute border border-[#cbd5e1] border-solid inset-0 pointer-events-none rounded-[24px]" />
              </div>
            </div>
            <div className="content-stretch flex gap-[12px] items-center py-[4px] relative shrink-0 w-full" data-name="Or Divider">
              <Separator />
              <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Container">
                <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[20px] justify-center leading-[0] not-italic relative shrink-0 text-[#64748b] text-[14px] w-[19.81px]">
                  <p className="leading-[20px]">OR</p>
                </div>
              </div>
              <Separator />
            </div>
            <div className="bg-[#f1f5f9] h-[48px] min-w-[84px] relative rounded-[24px] shrink-0 w-full" data-name="Secondary Button: Choose File → Button">
              <Wrapper>
                <div className="content-stretch flex flex-col items-center overflow-clip relative shrink-0" data-name="Container">
                  <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[#1e293b] text-[16px] text-center tracking-[0.24px] w-[91.06px]">
                    <p className="leading-[24px]">Choose file</p>
                  </div>
                </div>
              </Wrapper>
            </div>
          </div>
          <div className="-translate-y-1/2 absolute content-stretch flex items-start justify-center left-[24px] right-[24px] top-[calc(50%+183.75px)]" data-name="Primary CTA Button">
            <div className="bg-[#2badee] flex-[1_0_0] h-[56px] min-h-px min-w-[84px] relative rounded-[24px]" data-name="Button">
              <Wrapper>
                <div className="content-stretch flex flex-col items-center overflow-clip relative shrink-0" data-name="Container">
                  <div className="flex flex-col font-['Inter:Bold',sans-serif] font-bold h-[24px] justify-center leading-[0] not-italic relative shrink-0 text-[16px] text-center text-white tracking-[0.24px] w-[115.67px]">
                    <p className="leading-[24px]">Import playlist</p>
                  </div>
                </div>
              </Wrapper>
            </div>
          </div>
          <div className="-translate-y-1/2 absolute h-[16px] left-[24px] right-[24px] top-[calc(50%+219.75px)]" data-name="Feedback Area (Example: Error message)" />
          <div className="-translate-x-1/2 -translate-y-1/2 absolute left-1/2 size-0 top-[calc(50%+227.75px)]" data-name="Rectangle" />
          <div className="-translate-y-1/2 absolute content-stretch flex items-center justify-center left-[24px] pb-[16px] pt-[32px] right-[24px] top-[calc(50%+261.75px)]" data-name="Tertiary Link">
            <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="Link">
              <div className="flex flex-col font-['Inter:Medium',sans-serif] font-medium h-[20px] justify-center leading-[0] not-italic relative shrink-0 text-[#2badee] text-[14px] w-[118.75px]">
                <p className="leading-[20px]">Use demo playlist</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}